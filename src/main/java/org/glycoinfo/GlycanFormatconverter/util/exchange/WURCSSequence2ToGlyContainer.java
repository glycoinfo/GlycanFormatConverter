package org.glycoinfo.GlycanFormatconverter.util.exchange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.glycoinfo.GlycanFormatconverter.util.comparater.GlyCoModificationComparater;
import org.glycoinfo.GlycanFormatconverter.Glycan.AnomericStateDescriptor;
import org.glycoinfo.GlycanFormatconverter.Glycan.CrossLinkedTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyContainer;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanRepeatModification;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanUndefinedUnit;
import org.glycoinfo.GlycanFormatconverter.Glycan.Linkage;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlyCoModification;
import org.glycoinfo.GlycanFormatconverter.Glycan.ModificationTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.Monosaccharide;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.GlycanFormatconverter.Glycan.SubstituentInterface;
import org.glycoinfo.GlycanFormatconverter.Glycan.SubstituentTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.SuperClass;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;
import org.glycoinfo.WURCSFramework.util.array.WURCSImporter;
import org.glycoinfo.WURCSFramework.util.exchange.ConverterExchangeException;
import org.glycoinfo.WURCSFramework.util.subsumption.MSStateDeterminationUtility;
import org.glycoinfo.WURCSFramework.util.subsumption.WURCSSubsumptionConverter;
import org.glycoinfo.WURCSFramework.wurcs.array.LIP;
import org.glycoinfo.WURCSFramework.wurcs.array.LIPs;
import org.glycoinfo.WURCSFramework.wurcs.array.MOD;
import org.glycoinfo.WURCSFramework.wurcs.array.MS;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.BRIDGE;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.GLIN;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.GRES;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.SUBST;
import org.glycoinfo.WURCSFramework.wurcs.sequence2.WURCSSequence2;

public class WURCSSequence2ToGlyContainer {
	
	private HashMap<GRES, Node> gres2node = new HashMap<GRES, Node>();
	private GlyContainer glycan;
	private GlycanUndefinedUnit und;
	
	public GlyContainer getGlycan() {
		return glycan;
	}
	
	public void start (WURCSSequence2 _sequence2) throws WURCSFormatException, GlycanException, ConverterExchangeException {
		init();

		/* generate nodes */
		for(GRES gres : _sequence2.getGRESs()) {
			gres2node.put(gres, convertToNode(gres));
		}

		/* generate linkages */
		for(GRES gres : _sequence2.getGRESs()) {
			convertToLinkage(gres);			
		}
		
		/* generate ambiguous unit with substituent */
		extractAmbiguousSubstituent(_sequence2.getGRESs());
		
		/* add single node */
		if(glycan.getNodes().isEmpty() && _sequence2.getGRESCount() == 1) {
			for(GRES gres : _sequence2.getGRESs()) {
				glycan.addNode(gres2node.get(gres));
			}
		}
	}
	
	private void convertToLinkage (GRES _gres) throws GlycanException, WURCSFormatException {
		Node current = gres2node.get(_gres);
		Node parent = null;
		MS ms = new WURCSImporter().extractMS(_gres.getMS().getString());

		/* extract parent side */
		for(GLIN donor : _gres.getDonorGLINs()) {
			/* extract root of fragments */
			if (isAntennae(donor)) {
				und = new GlycanUndefinedUnit();
				Edge parentEdge = null;

				if (donor.getAcceptor().size() > 1) {
					parentEdge = GLINToLinkage(donor.getAcceptorPositions(), donor.getDonorPositions(), ms);
					parentEdge.setChild(current);
					for (GRES acceptor : donor.getAcceptor()) {
						und.addParentNode(gres2node.get(acceptor));
					}
				} else {
					current = gres2node.get(donor.getAcceptor().getFirst());

					if (glycan.containsAntennae(current)) {
						und = null;
						continue;
					}
					parentEdge = GLINToLinkage(donor.getDonorPositions(), donor.getAcceptorPositions(), ms);
					parentEdge.setChild(current);
					for (GRES dGRES : donor.getDonor()) {
						und.addParentNode(gres2node.get(dGRES));
					}
				}

				current.addParentEdge(parentEdge);
				und.setConnection(parentEdge);
				und.addNode(current);

			} else {
				Edge parentEdge = GLINToLinkage(donor.getAcceptorPositions(), donor.getDonorPositions(), ms);
				parent = gres2node.get(donor.getAcceptor().getFirst());
				
				/* extract cross-linked substituent */
				Substituent sub = MAPToSubstituent(donor);
				
				/* extract repeating unit and cyclic unit */
				if(donor.isRepeat() || isCyclic(_gres)) {
					CrossLinkedTemplate crossTemp = (CrossLinkedTemplate) MAPToInterface(donor.getMAP());
					GlycanRepeatModification repWithMod = new GlycanRepeatModification(crossTemp);
					repWithMod.setMaxRepeatCount(donor.getRepeatCountMax());
					repWithMod.setMinRepeatCount(donor.getRepeatCountMin());
					sub = repWithMod;
				}
				parentEdge.setSubstituent(sub);

				if(und != null) {
					if(und.containsNode(parent)) und.addNode(parent, parentEdge, current);
				} else
					glycan.addNode(parent, parentEdge, current);
			}

			/* add ambiguous unit */
			if(und != null && _gres.getAcceptorGLINs().isEmpty()) {
				glycan.addGlycanUndefinedUnit(und);
				und = null;
			}	
		}
		
		return;
	}
	
	private Edge GLINToLinkage (LinkedList<Integer> _acceptors, LinkedList<Integer> _donors, MS _ms) throws GlycanException {
		Edge edge = new Edge();
		Linkage lin = new Linkage();
		lin.setChildLinkages(_donors);
		lin.setParentLinkages(_acceptors);
		lin = extractProbabilityAnnotation(_ms, null, lin);

		edge.addGlycosidicLinkage(lin);
		
		return edge;
	}
	
	private Node convertToNode (GRES _gres) throws WURCSFormatException, GlycanException, ConverterExchangeException {
		Monosaccharide ret = new Monosaccharide();
		String skeletoncode = _gres.getMS().getCoreStructure().getSkeletonCode();
		MS ms = new WURCSImporter().extractMS(_gres.getMS().getString());

		/* extract superclass*/
		ret.setSuperClass(SuperClass.forSize(skeletoncode.length()));

		/* extract anomeric state */
		char anomericstate = _gres.getMS().getCoreStructure().getAnomericSymbol();
		AnomericStateDescriptor enumAnom = AnomericStateDescriptor.forAnomericState(checkAnomericState(skeletoncode, anomericstate));
		ret.setAnomer(enumAnom);
		
		/* extract anomeric position */
		int anomericposition = _gres.getMS().getCoreStructure().getAnomericPosition();
		if (enumAnom.equals(AnomericStateDescriptor.UNKNOWN)) anomericposition = Monosaccharide.UNKNOWN_RING;
		ret.setAnomericPosition(anomericposition);

		/* extract stereo */
		ret.setStereos(extractStereo(skeletoncode));
		
		/* extract ring position */
		for(BRIDGE bridge : _gres.getMS().getCoreStructure().getDivalentSubstituents()) {
			if(bridge.getMAP().equals("")) {
				if(bridge.getStartPositions().contains(new Integer(anomericposition))) {
					ret.setRing(bridge.getStartPositions().getFirst(), bridge.getEndPositions().getFirst());
				}else {
					ret = convertSubstituent(ret, BRIDGEToSubstituent(bridge, ms));
				}
			}else {
				ret = convertSubstituent(ret, BRIDGEToSubstituent(bridge, ms));
			}			
		}
		
		/* extract modification */
		for (int i = 0; i < skeletoncode.length(); i++) { 
			char carbon = skeletoncode.charAt(i);
			ModificationTemplate modT = ModificationTemplate.forCarbon(carbon);
			GlyCoModification mod;

			if (i > 0) {
				if (carbon == 'o') modT = ModificationTemplate.KETONE;
				if (carbon == 'O' || carbon == 'a') modT = ModificationTemplate.KETONE_U;
			}
			if (i == (ret.getSuperClass().getSize() - 1) && carbon == 'A') {
				modT = ModificationTemplate.URONICACID;
			}

			if(modT != null) {
				mod = new GlyCoModification(modT, i+1);
				ret.addModification(mod);

				if (i != 1 && i + 1 != ret.getSuperClass().getSize() &&
						(modT.equals(ModificationTemplate.KETONE) || modT.equals(ModificationTemplate.KETONE_U))) {
					mod = new GlyCoModification(modT, 1);
					ret.addModification(mod);
				}
			}
		}

		/* sort modifications */
		Collections.sort(ret.getModifications(), new GlyCoModificationComparater());

		/* extract single substituent from core */
		for (SUBST subst : _gres.getMS().getCoreStructure().getSubstituents()) {
			ret = convertSubstituent(ret, SUBSTToSubstituent(subst, ms));
		}
		
		/* extract single substituent from periferal */
		for (SUBST subst : _gres.getMS().getSubstituents()) {
			ret = convertSubstituent(ret, SUBSTToSubstituent(subst, ms));
		}
		
		/* extract divelent substituent from periferal */
		for (BRIDGE bridge : _gres.getMS().getDivalentSubstituents()) {
			ret = convertSubstituent(ret, BRIDGEToSubstituent(bridge, ms));
		}

		return ret;
	}
	
	private Monosaccharide convertSubstituent (Node _node, Node _substituent) throws GlycanException {
		Edge first = new Edge();
		first.setSubstituent(_substituent);
		first.setParent(_node);
		_node.addChildEdge(first);
		_substituent.addParentEdge(first);
		
		return (Monosaccharide) _node;
	}
	
	private void extractAmbiguousSubstituent (LinkedList<GRES> _gress) throws GlycanException, WURCSFormatException {
		GLIN temp = null;
		ArrayList<GLIN> indexes = new ArrayList<GLIN>();

		for (GRES gres : _gress) {
			MS ms = new WURCSImporter().extractMS(gres.getMS().getString());

			for (GLIN acceptor : gres.getAcceptorGLINs()) {
				if (acceptor.getMAP().equals("") || !acceptor.getDonor().isEmpty()) continue;
				if (temp != null && temp.equals(acceptor)) continue;
				if (indexes.contains(acceptor)) continue;

				temp = acceptor;

				Substituent sub = MAPToSubstituent(acceptor);
				Edge parentEdge = GLINToLinkage(acceptor.getAcceptorPositions(), acceptor.getDonorPositions(), ms);
				parentEdge.setChild(sub);

				und = new GlycanUndefinedUnit();
				und.setConnection(parentEdge);
				und.addNode(sub);

				for (GRES a : acceptor.getAcceptor()) {
					und.addParentNode(gres2node.get(a));
				}
				if (und != null) {
					glycan.addGlycanUndefinedUnit(und);
					und = null;
				}

				indexes.add(acceptor);
			}
		}
	}
	
	private char checkAnomericState (String _skeletoncode, char _anomericstate) {
		if (_anomericstate == 'o') {
			if (_skeletoncode.indexOf("o") == 0 || _skeletoncode.indexOf("O") == 1) return 'o';
			if (_skeletoncode.indexOf("u") == 0 || _skeletoncode.indexOf("U") == 1) return '?';
		}
		return _anomericstate;
	}
	
	private Substituent SUBSTToSubstituent(SUBST _subst, MS _ms) throws GlycanException {
		SubstituentInterface enumSub = MAPToInterface(_subst.getMAP());
		if (enumSub.getIUPACnotation().equals("")) {
			throw new GlycanException(_subst.getMAP() + " could not support in GlycanFormatConverter.");
		}
		Linkage lin = new Linkage();
		lin.addChildLinkage(1);
		lin.setParentLinkages(_subst.getPositions());
		lin = extractProbabilityAnnotation(_ms, _subst, lin);

		return new Substituent(enumSub, lin);
	}
	
	private Substituent BRIDGEToSubstituent(BRIDGE _bridge, MS _ms) throws GlycanException {
		String map = _bridge.getMAP().equals("") ? "*o" : _bridge.getMAP();

		SubstituentInterface cross = MAPToInterface(map);
		Linkage first = new Linkage();
		Linkage second = new Linkage();
		if (!_bridge.getMAP().equals("")) {
			first = extractModificationPosition(_bridge.getMAP(), _bridge.getStartPositions().getFirst(), _ms, first);
			second = extractModificationPosition(_bridge.getMAP(), _bridge.getEndPositions().getFirst(), _ms, second);
		} else {
			first.setParentLinkages(_bridge.getStartPositions());
			second.setParentLinkages(_bridge.getEndPositions());
		}

		return new Substituent(cross, first, second);
	}
	
	private Substituent MAPToSubstituent(GLIN _glin) throws GlycanException {
		if(_glin.getMAP().equals("")) return null;
		return new Substituent(MAPToInterface(_glin.getMAP()));
	}
	
	private SubstituentInterface MAPToInterface (String _map) throws GlycanException {
		if(_map.equals("")) return null;
		SubstituentInterface ret = null;
		if(SubstituentTemplate.forMAP(_map) != null) {
			ret = SubstituentTemplate.forMAP(_map);
		}
		if(CrossLinkedTemplate.forMAP(_map) != null) {
			ret = CrossLinkedTemplate.forMAP(_map);
		}
		if(ret == null) throw new GlycanException(_map +" could not found !");
		return ret;
	}
	
	private LinkedList<String> extractStereo(String _skeletoncode) throws WURCSFormatException, ConverterExchangeException {
		MS ms = new WURCSImporter().extractMS(_skeletoncode);
		MSStateDeterminationUtility msUtility = new MSStateDeterminationUtility();
		WURCSSubsumptionConverter wsc = new WURCSSubsumptionConverter();
		LinkedList<String> stereos = msUtility.extractStereo(ms);

		/* retry define stereo */
		if(stereos.isEmpty()) stereos = checkStereos(ms, msUtility, wsc);
		
		return stereos;
	}
	
	private LinkedList<String> checkStereos(MS _ms, MSStateDeterminationUtility _msUtility, WURCSSubsumptionConverter _wsc) throws ConverterExchangeException {
		LinkedList<String> a_aStereo = new LinkedList<String>();
		String a_sSkeletonCode = _ms.getSkeletonCode();
		SuperClass enumSuperClass = SuperClass.forSize(_ms.getSkeletonCode().length());

		if (a_sSkeletonCode.equals("<Q>")) {
			a_aStereo.addLast("Sugar");
		}

		if(a_sSkeletonCode.contains("1") && a_sSkeletonCode.contains("2")) {
			for(String s : 
				_msUtility.extractStereo(_wsc.convertConfigurationUnknownToAbsolutes(_ms).getFirst())) {
				if(a_sSkeletonCode.endsWith("xh") && s.contains("gro")) {
					a_aStereo.addLast(checkDLconfiguration(s));
					continue;
				}a_aStereo.addLast(s);
			}
		}

		if(a_sSkeletonCode.contains("3") || a_sSkeletonCode.contains("4")) {
			for (String ms : _msUtility.extractStereo(_wsc.convertConfigurationRelativeToD(_ms))) {
				if (ms.contains("gro")) {
					if (a_sSkeletonCode.endsWith("xh")) a_aStereo.addLast(checkDLconfiguration(ms));
					else a_aStereo.addLast(ms);
				} else a_aStereo.addLast(checkDLconfiguration(ms));
			}
				//a_aStereo = _msUtility.extractStereo(_wsc.convertConfigurationRelativeToD(_ms));
		}
		
		if(a_aStereo.isEmpty()) {
			if(enumSuperClass.equals(SuperClass.TET))
				throw new ConverterExchangeException(_ms.getSkeletonCode() + " could not handled");
			else if(enumSuperClass.equals(SuperClass.TRI)) {
				MS a_oGrose = _wsc.convertConfigurationUnknownToAbsolutes(_ms).getFirst();
				a_aStereo.add(checkDLconfiguration(_msUtility.extractStereo(a_oGrose).getFirst()));
			}else
				a_aStereo.add(enumSuperClass.getSuperClass().toLowerCase());				
		}

		return a_aStereo;
	}

	private Linkage extractProbabilityAnnotation (MS _ms, SUBST _subst, Linkage _linkage) {
		double low = 100.0;
		double high = 100.0;

		for(MOD mod : _ms.getMODs()) {
			if(_subst != null && (mod.getMAPCode().equals("") || !mod.getMAPCode().equals(_subst.getMAP())))
				continue;

			for (LIP lip : mod.getListOfLIPs().getFirst().getLIPs()) {
				if (lip.getBackbonePosition() != _linkage.getParentLinkages().get(0))
					continue;//_subst.getPositions().getFirst()) continue;
				if (lip.getModificationProbabilityLower() == 1.0 &&
						lip.getModificationProbabilityUpper() == 1.0) continue;

				if (_subst != null) {
					low = lip.getModificationProbabilityLower();
					high = lip.getModificationProbabilityUpper();
				} else {
					low = lip.getBackboneProbabilityLower();
					high = lip.getBackboneProbabilityUpper();
				}
			}
		}

		_linkage.setProbabilityLower(low);
		_linkage.setProbabilityUpper(high);

		return _linkage;
	}

	private Linkage extractModificationPosition (String _map, int _pos, MS _ms, Linkage _linkage) {
		for(MOD mod : _ms.getMODs()) {
			if(mod.getMAPCode().equals("") || !mod.getMAPCode().equals(_map))
				continue;

			for (LIPs lips : mod.getListOfLIPs()) {
				for (LIP lip : lips.getLIPs()) {
					if (_pos != lip.getBackbonePosition()) continue;
					_linkage.addChildLinkage(lip.getModificationPosition());
					_linkage.addParentLinkage(lip.getBackbonePosition());
				}
			}
		}

		return _linkage;
	}

	private String checkDLconfiguration(String a_sStereo) {
		if((a_sStereo.startsWith("l") || a_sStereo.startsWith("d"))) 
			a_sStereo = a_sStereo.replaceFirst("[ld]", "");
		return a_sStereo;
	}
	
	private boolean isCyclic (GRES _gres) {
		if(_gres.getID() > 1) return false;
		if(_gres.getAcceptorGLINs().isEmpty()) return false;

		for (GLIN donor : _gres.getDonorGLINs()) {
			for(GRES dGRES : donor.getAcceptor()) {
				if (donor.getDonorPositions().contains(_gres.getMS().getCoreStructure().getAnomericPosition()) &&
						donor.getAcceptorPositions().contains(dGRES.getMS().getCoreStructure().getAnomericPosition()))
					return false;
			}
		}

		return (!_gres.getAcceptorGLINs().getFirst().isRepeat());
	}

	private boolean isAntennae (GLIN _donor) {
		return (_donor.getAcceptor().size() > 1 || _donor.getDonor().size() > 1);
	}

	private void init() {
		glycan = new GlyContainer();
		gres2node.clear();
		und = null;
	}
}
