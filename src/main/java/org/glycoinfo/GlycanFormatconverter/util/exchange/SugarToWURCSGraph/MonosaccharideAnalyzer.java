package org.glycoinfo.GlycanFormatconverter.util.exchange.SugarToWURCSGraph;

import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.glycoinfo.WURCSFramework.util.exchange.WURCSExchangeException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

public class MonosaccharideAnalyzer {

	private Monosaccharide m_oMS;

	private boolean m_bIsAldose;

	private int m_iAnomPos;
	private char m_cAnomSymbol;
	private int m_nCAtom;
	private char m_cConfigurationalSymbol;
	private String m_strSkeletonCode;

	private LinkedList<Integer> m_aAnomPositions;
	private TreeMap<Integer, Character> m_hashPosToChar = new TreeMap<Integer, Character>();
	private LinkedList<String> m_aUnknownPosMAP;

	public MonosaccharideAnalyzer () {
		this.m_oMS = null;

		this.m_iAnomPos = 0;
		this.m_cAnomSymbol = 'x';
		this.m_nCAtom = 0;
		this.m_cConfigurationalSymbol = 'X';
		this.m_strSkeletonCode = "";

		this.m_bIsAldose = true;

		this.m_aUnknownPosMAP = new LinkedList<>();
		this.m_aAnomPositions = new LinkedList<>();
	}

	public int getAnomericPosition() {
		return this.m_iAnomPos;
	}

	public char getAnomericSymbol() {
		return this.m_cAnomSymbol;
	}

	public char getConfigurationalSymbol() {
		return this.m_cConfigurationalSymbol;
	}

	public int getNumberOfCarbons() {
		return this.m_nCAtom;
	}

	public String getSkeletonCode() {
		return this.m_strSkeletonCode;
	}

	public LinkedList<String> getCoreModificationUnknownPosition() {
		return this.m_aUnknownPosMAP;
	}

	public boolean isAldose() {
		return this.m_bIsAldose;
	}

	/**
	 * Analyze monosaccharide information
	 * @param a_oMS Target object of org.eurocarbdb.MolecularFramework.sugar.Monosaccharide, not {@code null}
	 * @throws WURCSExchangeException
	 */
	public void analyze(Monosaccharide a_oMS) throws WURCSExchangeException {
		this.m_oMS = a_oMS;

		this.m_iAnomPos    = a_oMS.getRingStart();
		this.m_cAnomSymbol = a_oMS.getAnomer().getSymbol().charAt(0);

		// Correct anomeric information
		if ( this.m_iAnomPos  == Monosaccharide.OPEN_CHAIN )
			this.m_cAnomSymbol = 'o';
		if ( this.m_cAnomSymbol == 'o' )
			this.m_iAnomPos = Monosaccharide.OPEN_CHAIN;

		this.m_nCAtom = a_oMS.getSuperclass().getCAtomCount();

		// Set hydroxyl group to head and tail position as default CarbonDescriptor
		this.m_hashPosToChar.put(1, 'h');
		this.m_hashPosToChar.put(this.m_nCAtom, 'h');

		// For modification
		LinkedList<Modification> t_aEnMods = new LinkedList<Modification>();
		for ( Modification t_oMod : this.m_oMS.getModification() ) {
			ModificationType t_oType = t_oMod.getModificationType();

			 // For modification with two likage position
			if(t_oMod.hasPositionTwo()) {
//				System.out.println( a_oMod.getPositionOne() +","+ a_oMod.getPositionTwo() +":"+ a_oMod.getName());
				// For (unknown) double bond
				if ( t_oType == ModificationType.DOUBLEBOND || t_oType == ModificationType.UNKNOWN_DOUBLEBOND ) {
					// After processing modification with single position,
					// to process double bond carbons for skeletoncharacter code
					t_aEnMods.add(t_oMod);
				}
				continue;
			}

			this.convertSingleModificationToCarbonDescriptor(t_oMod);
		}
		// For ketose
		if ( !this.m_aAnomPositions.isEmpty() && this.m_aAnomPositions.getFirst() != 1 )
			this.m_bIsAldose = false;

		// For head carbon
		if ( this.m_bIsAldose ) {
			this.m_hashPosToChar.put(1, 'o');
			this.m_aAnomPositions.addFirst(1);
		}
		// If no anomeric position, it must be open chain
		if ( this.m_aAnomPositions.isEmpty() ) {
			this.m_iAnomPos = Monosaccharide.OPEN_CHAIN;
			this.m_cAnomSymbol = 'o';
		}

		// Check anomeric position
		if ( this.m_iAnomPos != Monosaccharide.OPEN_CHAIN && this.m_iAnomPos != Monosaccharide.UNKNOWN_RING ) {
			if ( !this.m_hashPosToChar.containsKey(this.m_iAnomPos) )
				throw new WURCSExchangeException("Illegal structure is found at ring start position.");

			char t_cCD = this.m_hashPosToChar.get(this.m_iAnomPos);
			if ( t_cCD == 'o' || t_cCD == 'O' )
				this.m_hashPosToChar.put(this.m_iAnomPos, 'a');
/*
			t_cCD = (t_cCD == 'o')? 'c' :
					(t_cCD == 'O')? 'C' : t_cCD;
			this.m_hashPosToChar.put(this.m_iAnomPos, t_cCD);
*/
		}

		// For core modifications contained double bond
		for ( Modification t_oMod : t_aEnMods ) {
			if ( !this.replaceCarbonDescriptorByEnModification(t_oMod) )
				throw new WURCSExchangeException("There is an error in the modification \"en\" or \"enx\".");
		}
		// Count non terminal stereo
		int t_nBrankPosition = 0;
		for ( int i=2; i<this.m_nCAtom; i++) {
			if ( this.m_hashPosToChar.containsKey(i) ) continue;
			t_nBrankPosition++;
		}

		// For stereo code
		String t_strStereo = this.convertBasetypesToStereoCode(a_oMS.getBaseType());
//		System.out.println(t_strStereo+" vs "+t_nBrankPosition);
		if ( t_strStereo != "" && t_strStereo.length() != t_nBrankPosition )
			throw new WURCSExchangeException("There is the excess or shortage of the stereo information.");
		int j=0;
		for ( int i=2; i<this.m_nCAtom ; i++ ) {
			if ( this.m_hashPosToChar.containsKey(i) ) continue;
			char t_cCD = ( t_strStereo == "" )? 'x' : t_strStereo.charAt(j);
			this.m_hashPosToChar.put(i, t_cCD);
			j++;
		}

		// Set skeleton code
		for ( int i=0; i< this.m_nCAtom; i++ )
			this.m_strSkeletonCode += this.m_hashPosToChar.get(i+1);

	}

	private String convertBasetypesToStereoCode(ArrayList<BaseType> a_aBaseTypes) throws WURCSExchangeException {
		// Get stereocode
		String t_strStereoCode = "";
		LinkedList<String> dlList = new LinkedList<String>();
		for( BaseType bs : a_aBaseTypes ) {
			String code = bs.getStereoCode();
			// For relative configuration
			if ( bs.absoluteConfigurationUnknown() ) {
				// "1" and "2" are converted to "3" and "4"
				code = BaseTypeForRelativeConfiguration.forName(bs.getName()).getStereoCode();
			}
			if ( code.endsWith("1") ) dlList.add("L"); // "L" configuration
			if ( code.endsWith("2") ) dlList.add("D"); // "D" configuration
			t_strStereoCode = code + t_strStereoCode;
		}
//		stereo = stereo.replace("*", "X");
//		System.out.println(stereo);

		// Set D/L
		String dl = "X";
		if ( dlList.size() > 0 ) dl = dlList.getLast();
		this.m_cConfigurationalSymbol = dl.charAt(0);

		return t_strStereoCode;
	}

	private void convertSingleModificationToCarbonDescriptor(Modification a_oMod) throws WURCSExchangeException {
		// Modification with single position
		int t_iPos = a_oMod.getPositionOne();
		boolean t_bIsTerminal = (t_iPos == 1 || t_iPos == this.m_nCAtom);
		char t_cCD = this.convertModificationTypeToCarbonDescriptor(a_oMod.getModificationType());
		// For terminal deoxy
		if ( t_cCD == 'd' && t_bIsTerminal )
			t_cCD = 'm';
		// For ketose
		if ( t_cCD == 'O' ) {
			// Add candidate anomeric position
			this.m_aAnomPositions.add(t_iPos);
			// For terminal keto
			if (t_bIsTerminal) t_cCD = 'o';
		}

		if ( t_iPos == 1 )
			this.m_bIsAldose = false;

		// For unknown position
		if ( t_iPos == 0 ) {
			if ( t_cCD != 'd' )
				throw new WURCSExchangeException("Core modification at unknown position is not handled without DEOXY.");
			this.m_aUnknownPosMAP.add("*");
		}

		// Error check
		if ( t_cCD == 'h' && t_iPos != 1 )
			throw new WURCSExchangeException("Modification \"aldi\" is must set to first carbon.");
		if ( t_cCD == 'A' && !t_bIsTerminal )
			throw new WURCSExchangeException("Can not do carboxylation to non-terminal carbon.");
		if ( t_cCD == ' ' )
			throw new WURCSExchangeException("Unknown modification is found.");

		// Put CarbonDescriptor
		this.m_hashPosToChar.put(t_iPos, t_cCD);
	}

	private char convertModificationTypeToCarbonDescriptor(ModificationType a_oMod) {
		// For alditol
		if ( a_oMod == ModificationType.ALDI  ) return 'h';
		// For carbonyl acid
		if ( a_oMod == ModificationType.ACID  ) return 'A';
		// For deoxy
		if ( a_oMod == ModificationType.DEOXY ) return 'd';
		// For ketose modification
		if ( a_oMod == ModificationType.KETO  ) return 'O';
		return ' ';
	}

	/**
	 * Replace carbon descriptor by double bond core modification
	 * @param a_oMod A core modification
	 * @return True if no error in the replacement
	 */
	private boolean replaceCarbonDescriptorByEnModification(Modification a_oMod) {
		// pos1 < pos2
		int t_iPos1 = a_oMod.getPositionOne();
		int t_iPos2 = a_oMod.getPositionTwo();

		// For unknown position
		if ( t_iPos1 == 0 && t_iPos2 == 0 )
			this.m_aUnknownPosMAP.add("**");

		// Swap if at tail side terminal
		if ( t_iPos2 == this.m_nCAtom ) {
			int t_iPos = t_iPos2;
			t_iPos2 = t_iPos1;
			t_iPos1 = t_iPos;
		}
		// Check terminal
		boolean t_bAtTerminal = ( t_iPos1 == 1 || t_iPos1 == this.m_nCAtom );

		char t_cCD1 = ( this.m_hashPosToChar.containsKey(t_iPos1) )? this.m_hashPosToChar.get(t_iPos1) : ' ';
		char t_cCD2 = ( this.m_hashPosToChar.containsKey(t_iPos2) )? this.m_hashPosToChar.get(t_iPos2) : ' ';

		// Error check
		if ( t_cCD1 == 'o' || t_cCD1 == 'A' || t_cCD2 == 'O' || t_cCD2 == 'C' ) return false;

		/******* For cyclic form 1 : C1=C2 *********************************************
		 *  ?--C6               # C1 is anomeric carbon
		 *       \
		 *        C5--O5        X == H && Y == H -> C1 == 'h' -> 'z' && C2 == 'd' -> 'z'
		 *       /      \       X == H && Y == O -> C1 == 'h' -> 'z' && C2 == 'x' -> 'Z'
		 *  ?--C4        C1--X
		 *       \     //       X == O && Y == H -> C1 == 'c' -> 'N' && C2 == 'd' -> 'n'
		 *        C3--C2        X == O && Y == O -> C1 == 'c' -> 'N' && C2 == 'x' -> 'N'
		 *       /      \
		 *      ?        Y
		 ******************************************************************************/
		// For ring start at terminal
		if ( t_bAtTerminal &&  t_iPos1 == this.m_oMS.getRingEnd() ) {
			if ( t_cCD1 == 'c' ) {
				this.m_hashPosToChar.put(t_iPos1, 'N');
				this.m_hashPosToChar.put(t_iPos2, (t_cCD2 == 'd')? 'n':'N');
				return true;
			}
			if ( t_cCD1 == 'h' ) {
				this.m_hashPosToChar.put(t_iPos1, 'z');
				this.m_hashPosToChar.put(t_iPos2, (t_cCD2 == 'd')? 'z':'Z');
				return true;
			}
			return false;
		}

		/******* For cyclic form 2 : C5=C6 ********************************************
		 *   X                  # C5 is ring end carbon
		 *    \                 # Stereo of double bond between C5 and C6 is unknown
		 *     C6
		 *    /  X              # If X is not carbon (C6 is terminal)
		 *   Y    C5--O5        X == H && Y == H => C5 == 'x' -> 'F' && C6 == 'm' -> 'n'
		 *       /      \       X == H && Y == O => C5 == 'x' -> 'F' && C6 == 'h' -> 'F'
		 *  ?--C4        C1--?
		 *       \      /       # If X is carbon (C6 is not terminal)
		 *        C3--C2                  Y == H => C5 == 'x' -> 'F' && C6 == 'd' -> 'f'
		 *       /      \                 Y == O => C5 == 'x' -> 'F' && C6 == 'x' -> 'F'
		 *      ?        ?
		 ******************************************************************************/
		// For ring end at terminal and exo cyclic (Terminal is pos1)
		if ( t_bAtTerminal && t_iPos2 == this.m_oMS.getRingEnd() ) {
			// At terminal methyl
			if ( t_cCD1 == 'm' ) {
				this.m_hashPosToChar.put(t_iPos1, 'n');
				this.m_hashPosToChar.put(t_iPos2, (t_cCD2 == 'd')? 'n':'N');
				return true;
			}
			// At terminal hydroxy
			if ( t_cCD1 == 'h' ) {
				this.m_hashPosToChar.put(t_iPos1, 'f');
				this.m_hashPosToChar.put(t_iPos2, (t_cCD2 == 'd')? 'f':'F');
				return true;
			}
		}
		// For ring end at non terminal and exo cyclic
		if ( !t_bAtTerminal && t_iPos1 == this.m_oMS.getRingEnd() ) {
			this.m_hashPosToChar.put(t_iPos1, 'F');
			this.m_hashPosToChar.put(t_iPos2, (t_cCD2 == 'd')? 'f':'F');
			return true;
		}

		/******* For cyclic form 3 : C4=C5 ********************************************
		 *                      # C5 is ring end carbon
		 *      Y               # Stereo of double bond between C4 and C5 is E (entgegen)
		 *       \
		 *        C5--O5        # If Y is not carbon (C5 is terminal)
		 *      //      \       X == H && Y == H => C4 == 'd' -> 'z' && C5 == 'h' -> 'z'
		 *  X--C4        C1--?  X == O && Y == H => C4 == 'x' -> 'Z' && C5 == 'h' -> 'z'
		 *       \      /
		 *        C3--C2        # If Y is carbon (C5 is not terminal)
		 *       /      \       X == H           => C4 == 'd' -> 'e' && C5 == 'h' -> 'E'
		 *      ?        ?      X == O           => C4 == 'x' -> 'E' && C5 == 'h' -> 'E'
		 ******************************************************************************/
		// For ring end at terminal and endo cyclic (Terminal is pos1)
		if ( t_bAtTerminal && t_iPos1 == this.m_oMS.getRingEnd() ) {
			this.m_hashPosToChar.put(t_iPos1, 'z');
			this.m_hashPosToChar.put(t_iPos2, (t_cCD2 == 'd')? 'z':'Z');
			return true;
		}
		// For ring end at non terminal and endo cyclic
		if ( !t_bAtTerminal && t_iPos2 == this.m_oMS.getRingEnd() ) {
			this.m_hashPosToChar.put(t_iPos1, (t_cCD1 == 'd')? 'e':'E');
			this.m_hashPosToChar.put(t_iPos2, 'E');
			return true;
		}

		/******* For cyclic form 4 : C2=C3 *********************************************
		 *      ?               # stereo of double bond between C2 and C3 is Z (zusammen)
		 *       \
		 *        C5--O5
		 *       /      \       X == H  -> C2 == 'd' -> 'z'
		 *  ?--C4        C1--?  X == O  -> C2 == 'x' -> 'Z'
		 *       \      /
		 *        C3==C2        Y == H  -> C3 == 'd' -> 'z'
		 *       /      \       Y == O  -> C3 == 'x' -> 'Z'
		 *      Y        X
		 ******************************************************************************/
		// For end cyclic at non terminal
		if ( t_iPos1 > this.m_oMS.getRingStart() && t_iPos2 < this.m_oMS.getRingEnd() ) {
			this.m_hashPosToChar.put(t_iPos1, (t_cCD1 == 'd')? 'z':'Z');
			this.m_hashPosToChar.put(t_iPos2, (t_cCD2 == 'd')? 'z':'Z');
			return true;
		}

		/******* For open chain and exo cyclic *****************************************
		 *                  # stereo of double bond between C1 and C2 is unknown
		 *                  # X and Y can be exchange
		 *     ?     Y
		 *     |     |      X == H && Y == H  -> C1 == 'm' -> 'n'
		 *     ??    C1--X  X == O && Y == H  -> C1 == 'h' -> 'F' (e/z unknown)
		 *    /  \  X
		 *   ?    C2        Z == H -> C2 == 'f' , Z == O -> C2 == 'F' (e/z unknown)
		 *        |         Z == H -> C2 == 'n' , Z == O -> C2 == 'N' (no chirality)
		 *        Z
		 ******************************************************************************/
		// At terminal methyl
		if ( t_cCD1 == 'm' ) {
			this.m_hashPosToChar.put(t_iPos1, 'n');
			this.m_hashPosToChar.put(t_iPos2, (t_cCD2 == 'd')? 'n':'N');
			return true;
		}
		// At terminal hydroxy
		if ( t_cCD1 == 'h' ) {
			this.m_hashPosToChar.put(t_iPos1, 'f');
			this.m_hashPosToChar.put(t_iPos2, (t_cCD2 == 'd')? 'f':'F');
			return true;
		}
		// At non terminal
		this.m_hashPosToChar.put(t_iPos1, (t_cCD1 == 'd')? 'f':'F');
		this.m_hashPosToChar.put(t_iPos2, (t_cCD2 == 'd')? 'f':'F');
		return true;
	}
}
