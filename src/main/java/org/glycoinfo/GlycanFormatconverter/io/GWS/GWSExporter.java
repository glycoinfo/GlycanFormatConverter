package org.glycoinfo.GlycanFormatconverter.io.GWS;

import org.glycoinfo.GlycanFormatconverter.Glycan.*;
import org.glycoinfo.GlycanFormatconverter.io.ExporterInterface;
import org.glycoinfo.GlycanFormatconverter.util.similarity.NodeSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by e15d5605 on 2019/08/28.
 */
public class GWSExporter implements ExporterInterface {

    private HashMap<Node, String> nodeList;

    public GWSExporter () {
        this.nodeList = new HashMap<>();
    }

    public String start (GlyContainer _glyco) throws Exception {
        String ret = "";

        //TODO : sort all monosaccharides.
        //TODO : sort donor side nodes (containing substituent and modification).
        //TODO : define notation.
        //TODO : append acceptor side linkage.
        //TODO : append symbol of branches.
        //TODO : avoid to encode of core sustituent and modification.
        for (Edge edge : _glyco.getEdges()) {
            if (edge.getParent() != null) {
                this.makeMonosaccharideNotation(edge.getParent());
            }
            if (edge.getChild() != null) {
                this.makeMonosaccharideNotation(edge.getChild());
            }
            if (edge.getSubstituent() != null) {
                this.makeSubstituentNotation(edge.getSubstituent());
            }
        }

        //TODO : sort monosaccharide in monosaccharide compositions.
        if (_glyco.isComposition()) {

        } else {
            // sort core node
            NodeSimilarity nodeSim = new NodeSimilarity();
            ArrayList<Node> sortedList = nodeSim.sortAllNode(_glyco.getRootNodes().get(0));
            ret = this.makeSequence(sortedList);

            // sort fragments
            //TODO : sort root of fragments.
            //condensed.insert(0, makeFragmentsSequence(_glyCo.getUndefinedUnit()));
        }

        return ret;
    }

    @Override
    public void makeMonosaccharideNotation(Node _node) throws Exception {
        if (this.nodeList.containsKey(_node)) return;

        GWSNodeGenerator gwsNode = new GWSNodeGenerator();

        gwsNode.start(_node);

        String ret = gwsNode.getGWSNode();

        this.nodeList.put(_node, ret);

        this.makeLinkageNotation(_node);

        return;
    }

    @Override
    public void makeLinkageNotation(Node _node) {
        StringBuilder notation = new StringBuilder(this.nodeList.get(_node));

        if (_node instanceof Monosaccharide) {
            //TODO : sort acceptor side linkages with structure style.
            for (Edge acceptorEdge : _node.getParentEdges()) {
                for (Linkage lin : acceptorEdge.getGlycosidicLinkages()) {
                    notation.insert(0, this.integratePositions(lin.getParentLinkages()));
                }
            }

            //TODO : Append acceptor linkage position for non-reducing end.
            if (_node.getParentEdges().isEmpty()) {

            }
        }

        //Append linkage position of substituent.
        if (_node instanceof Substituent) {
            Substituent sub = ((Substituent) _node);

            //make linkage position with single bond substituent.
            //2?1S
            //DonorPosition : 1
            //LinkageType : ?
            //AcceptorPosition : 2
            if (sub.getSubstituent() instanceof BaseSubstituentTemplate) {
                for (Edge edge : sub.getParentEdges()) {
                    for (Linkage lin : edge.getGlycosidicLinkages()) {
                        //Append donor side linkage position
                        if (lin.getChildLinkages().contains(0)) {
                            notation.insert(0, 1);
                        } else {
                            notation.insert(0, this.integratePositions(lin.getChildLinkages()));
                        }

                        //Append linkage type
                        notation.insert(0, lin.getParentLinkageType().getSymbol());

                        //Append acceptor side linkage position
                        notation.insert(0, this.integratePositions(lin.getParentLinkages()));
                    }
                }
            }

            //make linkage position with multiple bond substituent.
            //2=1,5?2Anhydro
            //First linkage : 2=1, acceprot donor
            //Second linkage : 5?2, acceptor linkageType donor
            //? is linkage type
            if (sub.getSubstituent() instanceof BaseCrossLinkedTemplate) {
                for (Edge edge : sub.getParentEdges()) {
                    //Append second linkage
                    Linkage secondLin = sub.getSecondPosition();
                    if (secondLin.getChildLinkages().contains(0)) {
                        notation.insert(0, 2);
                    } else {
                        notation.insert(0, this.integratePositions(secondLin.getChildLinkages()));
                    }

                    notation.insert(0, secondLin.getChildLinkageType().getSymbol());

                    notation.insert(0, this.integratePositions(secondLin.getParentLinkages()));

                    notation.insert(0, ",");

                    //Append first linkage
                    Linkage firstLin = sub.getFirstPosition();
                    if (firstLin.getChildLinkages().contains(0)) {
                        notation.insert(0, 1);
                    } else {
                        notation.insert(0, this.integratePositions(firstLin.getChildLinkages()));
                    }

                    notation.insert(0, "=");

                    notation.insert(0, this.integratePositions(firstLin.getParentLinkages()));
                }
            }
        }

        notation.insert(0, "--");

        System.out.println(notation);

        this.nodeList.put(_node, notation.toString());
    }

    @Override
    public void makeSubstituentNotation(GlycanUndefinedUnit _und) throws Exception {

    }

    @Override
    public void makeFragmentsAnchor(GlyContainer _glyCo) throws Exception {

    }

    @Override
    public String makeComposition(GlyContainer _glyCo) {
        return null;
    }

    @Override
    public String makeSequence(ArrayList<Node> _nodes) throws Exception {
        StringBuilder ret = new StringBuilder("freeEnd");
        int branch = 0;

        //TODO : define type of reducing end.
        //TODO :
        NodeSimilarity nodeSim = new NodeSimilarity();

        for (Node node : _nodes) {
            ret.append(this.nodeList.get(node));
        }

        for(Node node : _nodes) {
            StringBuilder notation = new StringBuilder(this.nodeList.get(node));

            if(nodeSim.isMainChaineBranch(node)) {
                notation.append("]");
                branch++;
            }
            if(nodeSim.countChildren(node) == 0 && branch > 0) {
                notation.insert(0, "[");
                branch--;
            }
            ret.insert(0, notation);
        }

        return ret.toString();
    }

    @Override
    public String makeFragmentsSequence(ArrayList<GlycanUndefinedUnit> _fragments) throws Exception {
        return null;
    }

    private String integratePositions (ArrayList<Integer> _positions) {
        String ret = "";

        for (Iterator<Integer> iterPos = _positions.iterator(); iterPos.hasNext();) {
            int pos = iterPos.next();
            ret += (pos == -1) ? "?" : pos;
            if (iterPos.hasNext()) ret += ",";
        }

        return ret;
    }

    private void makeSubstituentNotation (Node _node) {
        String notation;

        GWSSubstituentGenerator gwsSub = new GWSSubstituentGenerator();
        gwsSub.start(_node);

        notation = gwsSub.getGWSSubNotation();

        this.nodeList.put(_node, notation);

        this.makeLinkageNotation(_node);

        return;
    }
}
