package org.glycoinfo.GlycanFormatconverter.Glycan;

import org.glycoinfo.GlycanFormatconverter.util.visitor.ContainerVisitor;
import org.glycoinfo.GlycanFormatconverter.util.visitor.VisitorException;

/**
 * Created by e15d5605 on 2018/12/13.
 */
public class Aglycone extends Node {

    private String name;

    public Aglycone (String _name) {
        this.name = _name;
    }

    public String getName () {
        return this.name;
    }

    @Override
    public void accept(ContainerVisitor _visitor) throws VisitorException {

    }

    @Override
    public Node copy() throws GlycanException {
        Aglycone copy = new Aglycone(this.getName());
        return copy;
    }
}
