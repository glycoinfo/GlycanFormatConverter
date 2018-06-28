package org.glycoinfo.GlycanFormatconverter.util.visitor;

public interface Visitable {
    public void accept (ContainerVisitor _visitor) throws VisitorException;
}
