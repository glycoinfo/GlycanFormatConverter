package org.glycoinfo.GlycanFormatconverter.util;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;

public class SubstituentUtility {

	private Substituent modifySimpleSubstituentLinkageType (Substituent _sub) throws GlycanException {
		if (_sub.getSubstituent() == null) return _sub;

		if (_sub.getFirstPosition() != null) {
			_sub.setFirstPosition(modifyLinkageType(_sub.getSubstituent(), _sub.getFirstPosition()));
		}
		if (_sub.getSecondPosition() != null) {
			_sub.setSecondPosition(modifyLinkageType(_sub.getSubstituent(), _sub.getSecondPosition()));
		}

		return _sub;
	}

	private Linkage modifyLinkageType (SubstituentInterface _subFace, Linkage _lin) throws GlycanException {
		if (isOLinkedSubstituent(_subFace)) {
			_lin.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
			_lin.setParentLinkageType(LinkageType.H_AT_OH);
		} else if (isMethylation(_subFace)) {
			_lin.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
			_lin.setParentLinkageType(LinkageType.H_LOSE);
		} else {
			_lin.setChildLinkageType(LinkageType.NONMONOSACCHARIDE);
			_lin.setParentLinkageType(LinkageType.UNVALIDATED);
		}

		return _lin;
	}

	private Substituent modifyBridgeSubstituentLinkageType (Substituent _sub) throws GlycanException {

		if (_sub.getFirstPosition() != null) {
			_sub.getFirstPosition().setChildLinkageType(LinkageType.DEOXY);
			_sub.getFirstPosition().setParentLinkageType(isOLinkedSubstituent(_sub.getSubstituent()) ?
					LinkageType.H_AT_OH : LinkageType.UNVALIDATED);
		}
		if (_sub.getSecondPosition() != null) {
			_sub.getSecondPosition().setChildLinkageType(LinkageType.DEOXY);
			_sub.getSecondPosition().setParentLinkageType(isOLinkedSubstituent(_sub.getSubstituent()) ?
					LinkageType.H_AT_OH : LinkageType.UNVALIDATED);
		}

		if (_sub.getFirstPosition() == null && _sub.getSecondPosition() == null) return _sub;

		/*
		if (!_sub.getFirstPosition().getChildLinkages().isEmpty() && !_sub.getSecondPosition().getChildLinkages().isEmpty()) {
			int pos1 = _sub.getFirstPosition().getChildLinkages().get(0);
			int pos2 = _sub.getSecondPosition().getChildLinkages().get(0);
			if (isSwap(pos1, pos2))
				_sub.getSecondPosition().setParentLinkageType(LinkageType.UNVALIDATED);
		}*/

		return _sub;
	}

	public Substituent modifyLinkageType (Substituent _sub) throws GlycanException {
		SubstituentInterface subFace = _sub.getSubstituent();

		if (subFace instanceof SubstituentTemplate) {
			return modifySimpleSubstituentLinkageType(_sub);
		}
		if (subFace instanceof CrossLinkedTemplate) {
			return modifyBridgeSubstituentLinkageType(_sub);
		}

		return _sub;
	}

	/*
	private boolean isSwap (Integer _firstChildPos, Integer _secondChildPos) {
		return (_firstChildPos > _secondChildPos);
	}
	*/

	public boolean isNLinkedSubstituent(SubstituentInterface _sub) {
		if (_sub instanceof CrossLinkedTemplate) return false;
		SubstituentTemplate subT = (SubstituentTemplate) _sub;
		return (subT.getMAP().startsWith("*N"));
	}

	public boolean isOLinkedSubstituent (SubstituentInterface _subFace) {
		if (_subFace instanceof CrossLinkedTemplate) {
			return isOLinkedSubstituent((CrossLinkedTemplate) _subFace);
		}
		if (_subFace instanceof SubstituentTemplate) {
			return isOLinkedSubstituent((SubstituentTemplate) _subFace);
		}

		return false;
	}

	public boolean isMethylation (SubstituentInterface _subFace) {
		if (_subFace instanceof SubstituentTemplate) {
			return isMethylation((SubstituentTemplate)  _subFace);
		}
		return false;
	}

	private boolean isOLinkedSubstituent (CrossLinkedTemplate _crossT) {
		if (_crossT.equals(CrossLinkedTemplate.PHOSPHOETHANOLAMINE)) return true;
		return (_crossT.getMAP().startsWith("*O") ||  _crossT.getMAP().startsWith("*1O"));
	}

	private boolean isOLinkedSubstituent (SubstituentTemplate _subT) {
		return (_subT.getMAP().startsWith("*O"));
	}

	private boolean isMethylation (SubstituentTemplate _subT) {
		return (//_subT.equals(SubstituentTemplate.C_METHYL) ||
				/*_subT.equals(SubstituentTemplate.N_METHYL) ||*/
				_subT.equals(SubstituentTemplate.HYDROXYMETHYL));
	}

	public SubstituentTemplate convertNTypeToOType(SubstituentInterface _sub) {
		SubstituentTemplate subT = (SubstituentTemplate) _sub;
		
		if(!this.isNLinkedSubstituent(_sub)) 
			return subT;		
		if(subT.equals(SubstituentTemplate.N_ACETYL)) 
			return SubstituentTemplate.ACETYL;
		if(subT.equals(SubstituentTemplate.N_AMIDINO)) 
			return SubstituentTemplate.AMIDINO;
		if(subT.equals(SubstituentTemplate.N_DIMETHYL)) 
			return SubstituentTemplate.DIMETHYL;
		if(subT.equals(SubstituentTemplate.N_FORMYL)) 
			return SubstituentTemplate.FORMYL;
		if(subT.equals(SubstituentTemplate.N_GLYCOLYL)) 
			return SubstituentTemplate.GLYCOLYL;
		if(subT.equals(SubstituentTemplate.N_METHYL)) 
			return SubstituentTemplate.METHYL;
		if(subT.equals(SubstituentTemplate.N_SUCCINATE)) 
			return SubstituentTemplate.SUCCINATE;
		if(subT.equals(SubstituentTemplate.N_SULFATE)) 
			return SubstituentTemplate.SULFATE;
		if(subT.equals(SubstituentTemplate.AMINE))
			return null;
		if(subT.equals(SubstituentTemplate.AMINO))
			return null;

		return subT;
	}
	
	public SubstituentTemplate convertOTypeToNType (SubstituentInterface _sub) {
		SubstituentTemplate subT = (SubstituentTemplate) _sub;
		
		if(subT.equals(SubstituentTemplate.ACETYL)) 
			return SubstituentTemplate.N_ACETYL;
		if(subT.equals(SubstituentTemplate.AMIDINO)) 
			return SubstituentTemplate.N_AMIDINO;
		if(subT.equals(SubstituentTemplate.DIMETHYL)) 
			return SubstituentTemplate.N_DIMETHYL;
		if(subT.equals(SubstituentTemplate.FORMYL)) 
			return SubstituentTemplate.N_FORMYL;
		if(subT.equals(SubstituentTemplate.GLYCOLYL)) 
			return SubstituentTemplate.N_GLYCOLYL;
		if(subT.equals(SubstituentTemplate.METHYL)) 
			return SubstituentTemplate.N_METHYL;
		if(subT.equals(SubstituentTemplate.SUCCINATE)) 
			return SubstituentTemplate.N_SUCCINATE;
		if(subT.equals(SubstituentTemplate.SULFATE)) 
			return SubstituentTemplate.N_SULFATE;
		if(subT.equals(SubstituentTemplate.AMINE))
			return null;
		if(subT.equals(SubstituentTemplate.AMINO))
			return null;

		return subT;
	}	
}
