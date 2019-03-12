package org.glycoinfo.GlycanFormatconverter.util.exchange.WURCSGraphToGlyContainer;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;

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

    public MAPAnalyzer () {
        this.baseTemp = null;
        this.headAtom = "";
        this.tailAtom = "";
    }

    //TODO : tailの処理もする必要がある
    //TODO : 後、架橋構造も

    public String getHeadAtom () {
        return this.headAtom;
    }

    public String getTailAtom () {
        return this.tailAtom;
    }

    public BaseSubstituentTemplate getSingleTemplate() {
        return this.baseTemp;
    }

    public BaseCrossLinkedTemplate getCrossTemplate () { return this.baseCrossTemp; }

    public void start (String _map) {
        String tempMAP = _map;//.substring(1, _map.length());

        // analyze MAP with H_AT_OH
        if (tempMAP.startsWith("O") && !tempMAP.contains("*")) {
            tempMAP = _map.substring(1, _map.length());
            this.headAtom = tempMAP.substring(0,1);
            tempMAP = tempMAP.replaceFirst(this.headAtom, "");
            tempMAP = removeOxygenFromHead(tempMAP);
        }

        this.baseTemp = BaseSubstituentTemplate.forMAP(tempMAP);

        if (baseTemp == null) {
            System.out.println(tempMAP);

            this.headAtom = tempMAP.substring(tempMAP.indexOf("*") + 1, tempMAP.indexOf("*") + 2);

            tempMAP = _map.substring(1, _map.length());

            this.tailAtom = tempMAP.substring(tempMAP.indexOf("*") - 1, tempMAP.indexOf("*"));

            System.out.println(this.headAtom + " " + this.tailAtom);

            tempMAP = this.removeOxygenFromail(tempMAP);

            System.out.println(tempMAP);

            this.baseCrossTemp = null;
        }

        return;
    }

    private String removeOxygenFromHead (String _map) {
        if (_map.startsWith("NCCOP")) return _map;

        ArrayList<Integer> nums = new ArrayList<Integer>();
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
            Integer num2 = num1 - 1;
            newMAP = newMAP.replaceAll(num1.toString(), num2.toString());
        }

        return "*" + newMAP;
    }

    private String removeOxygenFromail (String _map) {
        StringBuilder sb = new StringBuilder(_map);
        int pos = _map.lastIndexOf("*");
        //sb.insert(pos, 'O');
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
            posO--;
        }

        ArrayList<Integer> nums = new ArrayList<Integer>();
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

        newMAP = "*" + newMAP;

        newMAP = newMAP.replace("*OPO*", "*OP^XO*");
        newMAP = newMAP.replace("*P*", "*P^X*");

        return newMAP;
    }
}
