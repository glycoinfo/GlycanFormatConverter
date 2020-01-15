package org.glycoinfo.GlycanFormatconverter.io.GlycoCT;

import org.glycoinfo.GlycanFormatconverter.Glycan.CrossLinkedTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.SubstituentTemplate;
import org.glycoinfo.WURCSFramework.util.WURCSException;
import org.glycoinfo.WURCSFramework.util.WURCSFactory;
import org.glycoinfo.WURCSFramework.util.graph.visitor.WURCSVisitorCollectConnectingBackboneGroups;
import org.glycoinfo.WURCSFramework.wurcs.graph.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Class for validating WURCS to be converted to GlycoCT.
 * This class validate WURCS and find errors, warnings, errors for GlycoCT, and warnings for GlycoCT.
 * Errors contains error messages and the WURCS with the messages cannot be converted to the other formats.
 * Warnings contains warning messages but the WURCS with the messages can be converted to the other formats.
 * @author Masaaki Matsubara
 *
 */
public class WURCSConversionValidatorForGlycoCT {

	private List<String> m_lErrors;
	private List<String> m_lWarnings;
	private List<String> m_lErrorsForGlycoCT;
	private List<String> m_lWarningsForGlycoCT;
///	private List<String>

	public WURCSConversionValidatorForGlycoCT() {
		this.m_lErrors = new ArrayList<>();
		this.m_lWarnings = new ArrayList<>();
		this.m_lErrorsForGlycoCT = new ArrayList<>();
		this.m_lWarningsForGlycoCT = new ArrayList<>();
	}

	public List<String> getErrors() {
		return this.m_lErrors;
	}

	public List<String> getWarnings() {
		return this.m_lWarnings;
	}

	public List<String> getErrorsForGlycoCT() {
		return this.m_lErrorsForGlycoCT;
	}

	public List<String> getWarningsForGlycoCT() {
		return this.m_lWarningsForGlycoCT;
	}

	/**
	 * Starts validation.
	 * @param a_strWURCS String of WURCS to be validated
	 */
	public void start(String a_strWURCS) {
		try {
			WURCSFactory factory = new WURCSFactory(a_strWURCS);
			WURCSGraph graph = factory.getGraph();

			// Check # of backbone groups
			WURCSVisitorCollectConnectingBackboneGroups t_oGroup = new WURCSVisitorCollectConnectingBackboneGroups();
			t_oGroup.start(graph);
			if ( t_oGroup.getBackboneGroups().size() > 1 )
				this.m_lErrors.add("All residues must be connected.");

			// Validate Backbones
			for ( Backbone t_bb : graph.getBackbones() ) {
				// TODO: to increase capable carbon length to 12 or 20
				if ( t_bb.getLength() > 10 )
					this.m_lErrors.add("Carbon length must be 10 or less.");
				this.validateAnomer(t_bb);
				this.validateStereo(t_bb);
				this.validateAvailableCarbonDescriptor(t_bb);
			}

			// Validate Modifications
			for ( Modification t_mod : graph.getModifications() ) {
				// TODO: validate available substituents
				this.validateSubstituents(t_mod);
			}
			String t_strWURCS = factory.getWURCS();
		} catch (WURCSException e) {
			this.m_lErrors.add("WURCSException");
			e.printStackTrace();
		}

	}

	/**
	 * Validates anomer state of backbone. If there is error or something wrong, the messages are stored.
	 * @param a_bb Backbone to be checked anomer state
	 */
	private void validateAnomer(Backbone a_bb) {
		// Get anomer information from backbone carbons
		boolean t_bUncertainAnomer = false;
		boolean t_bCarbonyl = false;
		boolean t_bRingForm = false;
		int t_iAnomPos = 0;
		for ( BackboneCarbon t_bc : a_bb.getBackboneCarbons() ) {
			if ( t_bc.getDesctriptor().getChar() == 'u'
			  || t_bc.getDesctriptor().getChar() == 'U' )
				t_bUncertainAnomer = true;
			if ( t_bc.getDesctriptor().getChar() == 'o'
			  || t_bc.getDesctriptor().getChar() == 'O' )
				t_bCarbonyl = true;
			if ( t_bc.getDesctriptor().getChar() == 'a' ) {
				if ( t_bRingForm ) {
					this.m_lErrors.add("Anomer carbon must be only one in a monosaccharide.");
					continue;
				}
				t_iAnomPos = a_bb.getBackboneCarbons().indexOf(t_bc)+1;
				t_bRingForm = true;
			}
		}

		// Check combination of anomer state
		if ( t_bUncertainAnomer ) {
			this.m_lErrorsForGlycoCT.add("CarbonDescriptor \"u\" and \"U\" can not be handled in GlycoCT.");
			if ( t_bCarbonyl )
				this.m_lErrorsForGlycoCT.add("Carbonyl state will be lost in GlycoCT.");
			if ( t_bRingForm )
				this.m_lErrorsForGlycoCT.add("Ring state will be lost in GlycoCT.");
			t_iAnomPos = -1;
		}

		if ( t_bRingForm ) {
			// Validate anomer position
			if ( a_bb.getAnomericPosition() != t_iAnomPos )
				this.m_lErrors.add("Anomeric positions between the anomer descritpor 'a' and anomer information are mismathed.");
			// Validate ring existence
			if ( !this.hasRing(a_bb) )
				this.m_lErrors.add("Backbone with anomer descriptor 'a' must have a ring modification.");
		}
	}

	/**
	 * Validates stereo chemistry of backbone carbons.
	 * @param a_bb Backbone to be checked stereo chemistry
	 */
	private void validateStereo(Backbone a_bb) {
		// Analyze chiral stereo
		List<Integer> t_lH_LOSEPositions = new ArrayList<>();
		boolean t_bHasAbsolute = false;
		boolean t_bHasRelative = false;
		boolean t_bHasUnknown = false;
		int t_iLastStereo = -1;
		for ( BackboneCarbon t_bc : a_bb.getBackboneCarbons() ) {
			int t_iPos = a_bb.getBackboneCarbons().indexOf(t_bc)+1;
			// ignore terminal carbons
			if ( t_iPos == 1 || t_iPos == a_bb.getBackboneCarbons().size() )
				continue;
			char t_cd = t_bc.getDesctriptor().getChar();
			if ( t_cd == '1' || t_cd == '2' ) {
				t_bHasAbsolute = true;
			} else 
			if ( t_cd == '5' || t_cd == '6' ) {
				t_bHasAbsolute = true;
				t_lH_LOSEPositions.add(t_iPos);
			} else 
			if ( t_cd == '3' || t_cd == '4' ) {
				t_bHasRelative = true;
			} else
			if ( t_cd == '7' || t_cd == '8' ) {
				t_bHasRelative = true;
				t_lH_LOSEPositions.add(t_iPos);
			} else
			if ( t_cd == 'x' ) {
				t_bHasUnknown = true;
			} else
			if ( t_cd == 'X' ) {
				t_bHasUnknown = true;
				t_lH_LOSEPositions.add(t_iPos);
			} else
				continue;
			t_iLastStereo = t_iPos;
		}

		if ( !t_lH_LOSEPositions.isEmpty() ) {
			this.m_lErrorsForGlycoCT.add("Currently chiral carbons without hydrogen can not be handled.");
			// Check substituent on the carbon which hydrogen lose
			for ( int t_pos : t_lH_LOSEPositions ) {
				boolean t_bHasSubstituent = false;
				for ( WURCSEdge t_edge : a_bb.getEdges() ) {
					if ( t_edge.getLinkages().size() != 1 )
						continue;
					LinkagePosition t_link = t_edge.getLinkages().getFirst();
					if ( t_pos != t_link.getBackbonePosition() )
						continue;
					t_bHasSubstituent = true;
				}
				if ( !t_bHasSubstituent )
					this.m_lErrors.add("The chiral carbon without hydrogen must connect to at least one substituent.");
			}
		}
		if ( t_bHasUnknown && ( t_bHasRelative || t_bHasAbsolute ) )
			this.m_lErrorsForGlycoCT.add("Chiral information will be lost in GlycoCT.");

		// Analyze stereo on double bond
		t_bHasAbsolute = false;
		t_bHasUnknown = false;
		boolean t_bHasNoStereo = false;
		char t_cdPrev = ' ';
		for ( BackboneCarbon t_bc : a_bb.getBackboneCarbons() ) {
			int t_iPos = a_bb.getBackboneCarbons().indexOf(t_bc)+1;
			char t_cd = t_bc.getDesctriptor().getChar();
			if ( t_cd == 'e' || t_cd == 'E' || t_cd == 'z' || t_cd == 'Z' ) {
				t_bHasAbsolute = true;
			} else
			if ( t_cd == 'f' || t_cd == 'F' ) {
				t_bHasUnknown = true;
			} else
			if ( t_cd == 'n' || t_cd == 'N' ) {
				t_bHasNoStereo = true;
			} else
				t_cd = ' ';
			// Compare to previous CarbonDescriptor
			if ( t_cdPrev != ' ' && t_cd != ' ' ) {
				t_cd = Character.toLowerCase(t_cd);
				if ( t_cd != t_cdPrev )
					this.m_lErrors.add("Two carbons on a double bond must have the same stereo chemistry.");
				t_cd = ' ';
			}
			t_cdPrev = Character.toLowerCase(t_cd);
		}
		// TODO: To be handled 'n' and 'N'
		if ( t_bHasNoStereo )
			this.m_lErrors.add("CarbonDescriptor \'n\' and \'N\' can not be handled for now.");
		if ( t_bHasAbsolute && !this.hasRing(a_bb) )
			this.m_lErrorsForGlycoCT.add("Double bond stereo will be lost in GlycoCT.");

	}

	/**
	 * Validates that CarbonDescriptors in the Backbone are available or not for conversion.
	 * @param a_bb Backbone to be checked CarbonDescriptors
	 */
	private void validateAvailableCarbonDescriptor(Backbone a_bb) {
		for ( BackboneCarbon t_bc : a_bb.getBackboneCarbons() ) {
			int t_iPos = a_bb.getBackboneCarbons().indexOf(t_bc)+1;

			boolean t_bIsTerminal = false;
			if ( t_iPos == 1 || t_iPos == a_bb.getBackboneCarbons().size() )
				t_bIsTerminal = true;

			char t_cd = t_bc.getDesctriptor().getChar();
			if ( t_cd == 'c' || t_cd == 'C' || t_cd == 'M'
			  || t_cd == 't' || t_cd == 'T' || t_cd == 'K')
				this.m_lErrors.add("CarbonDescriptor "+t_cd+" can not be handled for now.");

			if ( !t_bIsTerminal )
				continue;

			// Stereo for terminal carbon
			if ( t_cd == '1' || t_cd == '2' || t_cd == '3' || t_cd == '4' || t_cd == 'x'
			  || t_cd == '5' || t_cd == '6' || t_cd == '7' || t_cd == '8' || t_cd == 'X')
				this.m_lErrors.add("Tarminal carbon with stereo can not be handled for now.");
		}

	}

	/**
	 * Returns true if the Backbone has an anomeric ring start with anomeric carbon.
	 * @param a_bb Backbone which may have a anomeric ring
	 * @return true if the Backbone has anomeric ring
	 */
	private boolean hasRing(Backbone a_bb) {
		Modification t_mod = this.getRing(a_bb);
		return (t_mod != null);
	}

	/**
	 * Gets an anomeric ring modification of the Backbone. Most prior one will be chosen if there are multiple ether rings start with anomeric carbon.
	 * Prior one is close to 6-memberd ring.
	 * @param a_bb Backbone which may have a anomeric ring
	 * @return Modification of a ring (most prior one will be chosen if there are multiple ether rings start with anomeric carbon, null if no ring)
	 */
	private Modification getRing(Backbone a_bb) {
		List<Modification> t_lRings = this.getRings(a_bb);
		if ( t_lRings.isEmpty() )
			return null;
		Collections.sort(t_lRings, new Comparator<Modification>(){

			@Override
			public int compare(Modification mod1, Modification mod2) {
				int t_iSize1 = getRingSize(mod1);
				int t_iSize2 = getRingSize(mod2);
				if ( t_iSize1 == t_iSize2 )
					return 0;
				// Non 0 comes first
				if ( t_iSize1 != 0 || t_iSize2 == 0 ) return -1;
				if ( t_iSize1 == 0 || t_iSize2 != 0 ) return 1;
				// Non -1 comes first
				if ( t_iSize1 != -1 || t_iSize2 == -1 ) return -1;
				if ( t_iSize1 == -1 || t_iSize2 != -1 ) return 1;
				// Prior closer size to six-membered
				int t_iComp = Math.abs(6-t_iSize1) - Math.abs(6-t_iSize2);
				if ( t_iComp != 0 )
					return t_iComp;
				// Prior smaller size
				return t_iSize1 - t_iSize2;
			}
			
		});
		// Return first
		return t_lRings.get(0);
	}

	/**
	 * Gets all anomeric ring modifications of the Backbone.
	 * @param a_bb Backbone which may have a ring modification.
	 * @return List of anomeric ring modifications connected to the Backbone.
	 */
	private List<Modification> getRings(Backbone a_bb) {
		List<Modification> t_lRings = new ArrayList<>();
		for ( WURCSEdge t_edge : a_bb.getChildEdges() ) {
			if ( !t_edge.getModification().isRing() )
				continue;
			t_lRings.add(t_edge.getModification());
		}
		return t_lRings;
	}

	/**
	 * Gets ring size of the Modification.
	 * @param a_mod Modification to be counted the ring size.
	 * @return The ring size of the Modification
	 */
	private int getRingSize(Modification a_mod) {
		if ( !a_mod.isRing() )
			return 0;
		WURCSEdge t_edgeStart = a_mod.getEdges().getFirst();
		WURCSEdge t_edgeEnd   = a_mod.getEdges().getLast();
		if ( t_edgeStart.getLinkages().size() != 1 || t_edgeEnd.getLinkages().size() != 1 )
			return -1;
		if ( t_edgeStart.getLinkages().getFirst().getBackbonePosition() == -1
		  || t_edgeEnd.getLinkages().getFirst().getBackbonePosition() == -1 )
			return -1;
		int t_iStart = t_edgeStart.getLinkages().getFirst().getBackbonePosition();
		int t_iEnd = t_edgeEnd.getLinkages().getFirst().getBackbonePosition();
		return Math.abs( t_iEnd - t_iStart ) + 1;
	}

	
	/**
	 * Validate the Modification is available for the other formats.
	 * @param a_mod Modification to be validated
	 */
	private void validateSubstituents(Modification a_mod) {
		// No error if the MAP can be omitted (empty "", "*O", "*", or "*=O")
		if ( a_mod.canOmitMAP() )
			return;

		String t_strMAP = a_mod.getMAPCode();
		int t_nStar = t_strMAP.length() - t_strMAP.replace("*", "").length();
		if ( t_nStar > 2 )
			this.m_lErrors.add("Substituent with three or more backbone carbons can not be handled.");
		if ( t_nStar == 0 )
			this.m_lErrors.add("At least one backbone carbon must be in a substituent.");
		if ( t_nStar == 1 && SubstituentTemplate.forMAP(t_strMAP) == null )
			this.m_lErrors.add("The substituent cannot be handled.");
		if ( t_nStar == 2 ) {
			if ( CrossLinkedTemplate.forMAP(t_strMAP) == null )
				this.m_lErrors.add("The closslinking substituent cannot be handled.");
			if ( a_mod.getEdges().size() != 2 )
				this.m_lErrors.add("The closslinking substituent must have two linkages.");
			if ( t_strMAP.startsWith("*1") ) {
				for ( WURCSEdge t_edge : a_mod.getEdges() ) {
					if ( t_edge.getLinkages().getFirst().getModificationPosition() == 0 )
						this.m_lErrors.add("The asymmetric closslinking substituent must have linkages with substituent linkage position.");
				}
			}
		}
	}
}
