package org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;

import org.glycoinfo.GlycanFormatconverter.Glycan.BaseCrossLinkedTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.BaseSubstituentTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.LinkageType;
import org.glycoinfo.WURCSFramework.util.array.WURCSFormatException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by e15d5605 on 2019/03/07.
 */
public class MAPAnalyzer {

    private BaseSubstituentTemplate baseTemp;
    private BaseCrossLinkedTemplate baseCrossTemp;
    private String headAtom;
    private String tailAtom;
    private String headPos;
    private String tailPos;

    public MAPAnalyzer () {
        this.baseTemp = null;
        this.headAtom = "";
        this.tailAtom = "";
        this.headPos = "";
        this.tailPos = "";
    }

    public String getHeadAtom () {
        return this.headAtom;
    }

    public String getTailAtom () { return this.tailAtom; }

    public BaseSubstituentTemplate getSingleTemplate() {
        return this.baseTemp;
    }

    public BaseCrossLinkedTemplate getCrossTemplate () { return this.baseCrossTemp; }

    public void start (String _map) throws WURCSFormatException {
        if (_map.equals("")) return;

        String tempMAP = _map;

        // an hydro
        this.baseCrossTemp = BaseCrossLinkedTemplate.forMAP(_map);
        if (this.baseCrossTemp != null && this.baseCrossTemp.equals(BaseCrossLinkedTemplate.ANHYDRO)) {
            return;
        }

        if (isMAPOfSingleLinkage(_map)) {
            tempMAP = tempMAP.replaceFirst("\\*", "");
        }

        // analyze MAP with H_AT_OH
        if (!tempMAP.contains("*") && _map.startsWith("*O")) {
            this.headAtom = tempMAP.substring(0, 1);
            tempMAP = removeOxygenFromHead(tempMAP);
        }

        this.baseTemp = BaseSubstituentTemplate.forMAP(tempMAP);
        if (this.baseTemp != null) {
            if (baseTemp.equals(BaseSubstituentTemplate.AMINE)) this.headAtom = "N";
            return;
        }

        // make base MAP of double linkage substituent
        //*N*, *S*, *(CCC^ZCC^EC$2)/6NSC/9=O/9=O/3O*
        //if (_map.equals("*N*")) {
        if (_map.matches("\\*[A-Z]\\*.*")) {
            tempMAP = tempMAP.replaceFirst("\\*", "");
            this.baseCrossTemp = BaseCrossLinkedTemplate.forMAP(tempMAP);
            return;
        }

        // extract deoxy position
        tempMAP = this.extractHeadPosition(tempMAP);
        tempMAP = this.extractTailPosition(tempMAP);

        // extract head atom from MAP
        this.headAtom = String.valueOf(tempMAP.charAt(tempMAP.indexOf("*") + 1));

        // extract tail atom from MAP
        if (this.countStar(tempMAP) > 1)
            this.tailAtom = String.valueOf(tempMAP.charAt(tempMAP.lastIndexOf("*") - 1));

        tempMAP = tempMAP.replaceFirst("\\*", "");

        // remove tail oxygen from MAP
        tempMAP = this.makeDoubleLinkMAP(tempMAP);

        if (tempMAP.startsWith("*")) {
            tempMAP = tempMAP.substring(1);
        }

        this.baseCrossTemp = BaseCrossLinkedTemplate.forMAP(tempMAP);
    }

    private String makeDoubleLinkMAP (String _map) {
        String ret = _map;

        // Set map positions and linkage type
        Boolean isSwap = null;
        boolean hasOrder = false;
        if (!this.headAtom.equals(this.tailAtom)) {
            if (this.headAtom.equals("O")) {
                isSwap = false;
            } else if (this.tailAtom.equals("O")) {
                isSwap = true;
            }
        }

        if (isSwap != null) {
            hasOrder = true;
        } else {
            isSwap = false;
        }

        // Remove oxygen
        if (this.checkLinkageTypeWithAtom(headAtom).equals(LinkageType.H_AT_OH)) {
            ret = (isSwap) ? this.removeOxygenFromTail(ret) : this.removeOxygenFromHead(ret);
        }
        if (this.checkLinkageTypeWithAtom(tailAtom).equals(LinkageType.H_AT_OH)) {
            ret = (isSwap) ? this.removeOxygenFromHead(ret) : this.removeOxygenFromTail(ret);
        }

        // Add index to MAP star if it has priority order
        if (hasOrder) {
           //ret = this.addMAPStarIndex(ret);
        }

        ret = ret.replace("P*", "P^X*");

        return ret;
    }

    private String removeOxygenFromHead (String _map) {
        if (_map.startsWith("NCCOP")) return _map;

        ArrayList<Integer> nums = new ArrayList<>();
        String num = "";
        for (int i = 0; i < _map.length(); i++) {
            char c = _map.charAt(i);
            if (Character.isDigit(c)) {
                num += c;
                continue;
            }
            if (num.equals("")) continue;
            if (nums.contains(Integer.parseInt(num))) continue;
            nums.add(Integer.parseInt(num));
            num = "";
        }
        Collections.sort(nums);
        //Collections.reverse(nums);

        String newMAP = _map;
        for (Iterator<Integer> iterNum = nums.iterator(); iterNum.hasNext();) {
            Integer num1 = iterNum.next();
            Integer num2 = num1 - 1;
            newMAP = newMAP.replaceAll(num1.toString(), num2.toString());
        }

        // Remove head oxygen
        if (this.headAtom.equals("O"))
            newMAP = newMAP.replaceFirst(this.headAtom, "");

        return newMAP;
    }

    private String removeOxygenFromTail (String _map) {
        // Remove "O" to MAP code before last "*"
        StringBuilder sb = new StringBuilder(_map);
        int pos = _map.lastIndexOf("*");
        sb.replace(pos-1, pos, "");
        _map = sb.toString();

        int posO = 1;
        for (int i=0; i < pos; i++) {
            char c = _map.charAt(i);
            if (c == '^' || c == '/') {
                i++;
                continue;
            } else if (c == '=' || c == '#') {
                continue;
            } else if (c == '*') {
                break;
            }
            posO++;
        }

        ArrayList<Integer> nums = new ArrayList<>();
        String num = "";
        for (int i = 0; i < _map.length(); i++) {
            char c = _map.charAt(i);
            if (Character.isDigit(c)) {
                num += c;
                continue;
            }
            if (num.equals("")) continue;
            if (nums.contains(Integer.parseInt(num))) continue;
            nums.add(Integer.parseInt(num));
            num = "";
        }
        Collections.sort(nums);
        Collections.reverse(nums);

        String newMAP = _map;
        for (Iterator<Integer> iterNum = nums.iterator(); iterNum.hasNext();) {
            Integer num1 = iterNum.next();
            if (num1 <= posO) continue;
            Integer num2 = num1 - 1;
            newMAP = newMAP.replaceAll(num1.toString(), num2.toString());
        }

        return newMAP;
    }

    private boolean isMAPOfSingleLinkage (String _map) {
        int ret = 0;
        for (int i = 0; i < _map.length(); i++) {
            char item = _map.charAt(i);
            if (item == '*') ret++;
        }

        return (ret == 1);
    }

    private String extractHeadPosition (String _map) {
        int pos = _map.indexOf("*");

        if (pos != 0) return _map;

        this.headPos = _map.substring(pos+1, pos+2);

        if (headPos.matches("[1-9]")) {
            _map = _map.replaceFirst(headPos, "");
        } else {
            headPos = "";
        }

        return _map;
    }

    private String extractTailPosition (String _map) {
        int pos = _map.lastIndexOf("*");
        StringBuilder ret = new StringBuilder(_map);

        if (pos == -1) return _map;

        if (_map.length() == (pos + 1)) return "";

        this.tailPos = _map.substring(pos+1, pos+2);

        if (tailPos.matches("[1-9]")) {
            ret.replace(pos+1, pos+2, "");
        } else {
            tailPos = "";
        }

        return ret.toString();
    }

    private LinkageType checkLinkageTypeWithAtom (String _atom) {
        if (_atom.equals("O") || _atom.equals("N")) {
            return LinkageType.H_AT_OH;
        }
        return LinkageType.DEOXY;
    }

    private int countStar (String _map) {
        int ret = 0;
        for (String s : _map.split("")) {
            if (s.equals("*")) ret++;
        }
        return ret;
    }
}
