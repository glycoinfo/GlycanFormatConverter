package org.glycoinfo.GlycanFormatConverter.IUPAC;

import java.util.ArrayList;

import org.glycoinfo.GlycanFormatconverter.Glycan.CrossLinkedTemplate;
import org.glycoinfo.GlycanFormatconverter.Glycan.Edge;
import org.glycoinfo.GlycanFormatconverter.Glycan.GlycanException;
import org.glycoinfo.GlycanFormatconverter.Glycan.Node;
import org.glycoinfo.GlycanFormatconverter.Glycan.Substituent;
import org.glycoinfo.GlycanFormatconverter.io.GlyCoExporterException;
import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.glycoinfo.WURCSFramework.util.WURCSException;
//import org.glycoinfo.subsumption.SubsumptionException;
import org.junit.Test;

/**
 * Created by e15d5605 on 2018/01/19.
 */
public class WURCSToIUPAC_single {

    @Test
    public void WURCSToIUPAC () throws GlycanException, WURCSException, GlyCoExporterException {//, SubsumptionException {

        ArrayList<String> sets = new ArrayList<>();
        
        //sets.add("WURCS=2.0/1,1,0/[ha122h-2x_2-5_6*OPO/3O/3=O]/1/");
        //sets.add("WURCS=2.0/5,17,16/[AUd21122h_5*NCC/3=O][AUd21122h_5*NCCO/3=O][u2122h_2*NCC/3=O][u1122h][u2112h]/1-1-1-2-3-3-3-3-3-3-4-4-4-5-5-5-5/a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?");
        //sets.add("WURCS=2.0/5,11,10/[a2122h-1x_1-5_2*NCC/3=O][a1122h-1x_1-5][a2112h-1x_1-5][Aad21122h-2x_2-6_5*NCC/3=O][Aad21122h-2x_2-6_5*NCCO/3=O]/1-1-2-2-1-3-4-2-1-3-5/a?-b1_b?-c1_c?-d1_c?-h1_d?-e1_e?-f1_f?-g2_h?-i1_i?-j1_j?-k2");
        //sets.add("WURCS=2.0/1,3,4/[ha122h-2b_2-5]/1-1-1/a1-b2_a2-c1_b1-c2_c1-c2~4:6");
        //sets.add("WURCS=2.0/3,3,2/[a2x12h-1b_1-5][a2112h-1b_1-5][a2112h-1x_1-5]/1-2-3/a3-b1_b3-c1");
        //sets.add("WURCS=2.0/1,3,4/[ha122h-2b_2-5]/1-1-1/a1-b4_a4-c1_b1-c4_c1-c4~4:5");
        //sets.add("WURCS=2.0/3,8,8/[h2h][a2122h-1a_1-5_2*NCC/3=O][a2122h-1a_1-5]/1-1-2-1-3-1-3-1/a1-b3*OPO*/3O/3=O_a3-d1*OPO*/3O/3=O_b1-h3*OPO*/3O/3=O_b2-c1_d2-e1_d3-f1*OPO*/3O/3=O_f2-g1_f3-h1*OPO*/3O/3=O~n");
        //sets.add("WURCS=2.0/1,1,0/[A2Ch_1-4_3*C_3*C]/1/");
        //sets.add("WURCS=2.0/1,1,0/[a2122h-1b_1-5_2*NCC/3=O_3*OC^XCO/4=O/3C]/1/");
        //sets.add("WURCS=2.0/3,4,3/[u44h][a2112h-1x_1-5_2*NCC/3=O][a2122h-1x_1-5_2*NCC/3=O_3*OCC/3=O_4*OCC/3=O]/1-2-2-3/a1-b1_b3-c1_b4-d1");
        //sets.add("WURCS=2.0/1,1,0/[hOh_1*OPO/3O/3=O_3*OCCCCCCCCCCCCCCCCCC]/1/");
        //sets.add("WURCS=2.0/1,1,0/[odh_3*OPNCCCl/3NCCCl/3=O]/1/");
        //sets.add("WURCS=2.0/3,6,5/[o222h][a222h-1b_1-4][a222h-1x_1-4]/1-2-2-3-2-3/a1-b1_b3-f5*OPO*/3O/3=O_c1-d1_d5-e3*OPO*/3O/3=O_e1-f1");
        //sets.add("WURCS=2.0/1,14,13/[ad22h-1b_1-4_1*N]/1-1-1-1-1-1-1-1-1-1-1-1-1-1/a3-b5*OPO*/3O/3=O_a5-c3*OPO*/3O/3=O_c5-k3*OPO*/3O/3=O_d3-j5*OPO*/3O/3=O_e3-f5*OPO*/3O/3=O_e5-i3*OPO*/3O/3=O_f3-l5*OPO*/3O/3=O_g3-k5*OPO*/3O/3=O_g5-l3*OPO*/3O/3=O_h3-n5*OPO*/3O/3=O_h5-j3*OPO*/3O/3=O_i5-m3*OPO*/3O/3=O_m5-n3*OPO*/3O/3=O");
        //sets.add("WURCS=2.0/1,12,11/[ad22h-1b_1-4_1*N]/1-1-1-1-1-1-1-1-1-1-1-1/a3-b5*OPO*/3O/3=O_a5-c3*OPO*/3O/3=O_c5-i3*OPO*/3O/3=O_d3-l5*OPO*/3O/3=O_e3-g5*OPO*/3O/3=O_e5-h3*OPO*/3O/3=O_f3-i5*OPO*/3O/3=O_f5-k3*OPO*/3O/3=O_g3-k5*OPO*/3O/3=O_h5-j3*OPO*/3O/3=O_j5-l3*OPO*/3O/3=O");
        //sets.add("WURCS=2.0/1,7,6/[a222h-1b_1-4_1*N]/1-1-1-1-1-1-1/a3-b5*OPO*/3O/3=O_a5-c3*OPO*/3O/3=O_b3-f5*OPO*/3O/3=O_d5-g3*OPO*/3O/3=O_e3-g5*OPO*/3O/3=O_e5-f3*OPO*/3O/3=O");
        //sets.add("WURCS=2.0/2,4,3/[a122h-1x_1-4][<Q>]/1-1-2-2/a5-b1_b3-c1_b5-d1");
        //sets.add("WURCS=2.0/1,1,0/[ad2521d1m-1x_1-4*=N*_4-8_5n2-6n1*1CCCCCCC(CNC^ZCC$9)/11*2_1*C_3*OC]/1/");
        //sets.add("WURCS=2.0/2,2,1/[a222h-1b_1-4_1*N][a2111m-1b_1-5_2*NCC/3=O_4*N]/1-2/a5-b1*OP^XOP^XO*/5O/5=O/3O/3=O");
        //sets.add("WURCS=2.0/1,1,0/[Ad1zz11h_3-7_1*NCCCCC$2_6*NCNCC/5C/3=O]/1/");
        //sets.add("WURCS=2.0/1,1,0/[zz2h-1-4_3*C]/1/");
        //sets.add("WURCS=2.0/3,4,4/[a1122m-1a_1-5_3%.65%*OC][a1122m-1a_1-5][a2112m-1a_1-4]/1-2-2-3/a2-b1_b3-c1_c4-d1_a1-c3~n");
        //sets.add("WURCS=2.0/1,1,1/[a2122h-1a_1-5]/1/a1-a3~n");
        //sets.add("WURCS=2.0/3,4,4/[Aad21121m-2b_2-6_5*N_7*N_7%.65%*OCC/3=O][a2122h-1a_1-5_2*NCC/3=O][a1221m-1a_1-5_2*NCC/3=O]/1-2-3-2/a4-b1_b3-c1_c3-d1_a1-d6~n");
		//sets.add("WURCS=2.0/5,17,16/[AUd21122h_5*NCC/3=O][AUd21122h_5*NCCO/3=O][u2122h_2*NCC/3=O][u1122h][u2112h]/1-1-1-2-3-3-3-3-3-3-4-4-4-5-5-5-5/a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?}-{a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?|m?|n?|o?|p?|q?");
        //sets.add("WURCS=2.0/3,8,7/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1a_1-5]/1-2-3-3-2-2-3-2/a4-b1_b4-c1_c3-d1_c4-f1_c6-g1_d2-e1_g2-h1");
        //sets.add("WURCS=2.0/3,6,5/[a2122h-1x_1-5_2*NCC/3=O][a2112h-1x_1-5][a2112h-1x_1-5_2*NCC/3=O]/1-2-1-2-3-2/a?-b1_c?-d1_e?-f1_a1-e?|f?}_c1-e?|f?}");
        //sets.add("WURCS=2.0/1,4,4/[a2122h-1a_1-5]/1-1-1-1/a1-d6_a6-b1_b6-c1_c6-d1");
        //sets.add("WURCS=2.0/3,9,0+/[axxxxh-1x_1-5_?*][a2122h-1x_1-5_2*NCC/3=O][axxxxh-1x_1-5]/1-2-2-3-3-3-3-3-3/");
        //sets.add("WURCS=2.0/6,15,14/[a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a1221m-1a_1-5][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-1-2-3-3-4-1-4-5-6-1-4-5-1-5/a4-b1_a6-f1_b4-c1_c3-d1_c6-e1_g3-h1_g4-i1_k3-l1_k4-m1_n4-o1_i?-j2_g1-a?|b?|c?|d?|e?|f?}_k1-a?|b?|c?|d?|e?|f?}_n1-a?|b?|c?|d?|e?|f?}");
        //sets.add("WURCS=2.0/6,11,10/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][a2112h-1b_1-5][a1221m-1a_1-5]/1-2-3-4-2-5-4-2-6-2-5/a4-b1_a6-i1_b4-c1_c3-d1_c6-g1_d2-e1_e4-f1_g2-h1_j4-k1_j1-d4|d6|g4|g6}");
        //sets.add("WURCS=2.0/7,12,14/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5][axxxxh-1x_1-?_2*NCC/3=O][a2112h-1b_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-4-5-6-7-7-4-5-6-7/a4-b1_b4-c1_e4-f1_f3-g2_g8-h2_j4-k1_k3-l2_c?-d1_c?-i1_d?-e1_i?-j1_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}*OCC/3=O_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}*OCC/3=O_a?|b?|c?|d?|e?|f?|g?|h?|i?|j?|k?|l?}*OCC/3=O");
        //sets.add("WURCS=2.0/4,5,4/[a2122h-1a_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5]/1-2-3-4-4/a4-b1_b4-c1_c3-d1_c6-e1");
        //sets.add("WURCS=2.0/4,5,4/[a2122h-1a_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5]/1-2-3-4-4/a4-b1_b4-c1_c3-d1_c6-e1");
        //sets.add("WURCS=2.0/1,4,4/[a1122h-1a_1-5]/1-1-1-1/a1-c3_a4-b1_b4-c1_c4-d1");
        //sets.add("WURCS=2.0/3,8,8/[h2h][a2122h-1a_1-5_2*NCC/3=O][a2122h-1a_1-5]/1-1-2-1-3-1-3-1/a1-b3*OPO*/3O/3=O_a3-d1*OPO*/3O/3=O_b1-h3*OPO*/3O/3=O_b2-c1_d2-e1_d3-f1*OPO*/3O/3=O_f2-g1_f3-h1*OPO*/3O/3=O~n");
        //sets.add("WURCS=2.0/3,8,8/[h2h][a2122h-1a_1-5_2*NCC/3=O][a2122h-1a_1-5]/1-1-2-1-3-1-1-3/a1-b3*OPO*/3O/3=O_a3-d1*OPO*/3O/3=O_b1-f3*OPO*/3O/3=O_b2-c1_d2-e1_d3-g1*OPO*/3O/3=O_g2-h1_f1-g3*OPO*/3O/3=O~n");
        //sets.add("WURCS=2.0/3,8,8/[h2h][a2122h-1a_1-5_2*NCC/3=O][a2122h-1a_1-5]/1-1-2-1-3-1-3-1/a1-b3*OPO*/3O/3=O_a3-d1*OPO*/3O/3=O_b1-h3*OPO*/3O/3=O_b2-c1_d2-e1_d3-f1*OPO*/3O/3=O_f2-g1_f3-h1*OPO*/3O/3=O~n");
        //sets.add("WURCS=2.0/3,8,8/[h2h][a2122h-1a_1-5_2*NCC/3=O][a2122h-1a_1-5]/1-1-2-1-3-1-1-3/a1-b3*OPO*/3O/3=O_a3-d1*OPO*/3O/3=O_b1-f3*OPO*/3O/3=O_b2-c1_d2-e1_d3-g1*OPO*/3O/3=O_g2-h1_f1-g3*OPO*/3O/3=O~n");
        //sets.add("WURCS=2.0/4,5,4/[u2122h_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5]/1-2-3-4-4/a4-b1_b4-c1_c3-d1_c6-e1");
        	//sets.add("WURCS=2.0/5,8,7/[a2122h-1x_1-5_2*N][a2122h-1b_1-5_2*N][Aad1122h-2b_2-6][Aad1122h-2x_2-6][axxxxxh-1x_1-?]/1-2-3-4-5-5-5-5/a6-b1_b6-c2_c?-d2_c?-g?_e?-h?_f?-h?_g?-h?");
        	//sets.add("WURCS=2.0/3,8,7/[a2122h-1x_1-5_2*N][Aad1122h-2x_2-6][axxxxxh-1x_1-?]/1-1-2-2-3-3-3-3/a?-b1_b?-c2_c?-d2_c?-g?_e?-h?_f?-h?_g?-h?");
        //sets.add("WURCS=2.0/1,5,5/[a2122h-1a_1-5]/1-1-1-1-1/a1-d3_a6-b1_b3-c1_c6-d1_d6-e1");	
        //sets.add("WURCS=2.0/3,3,2/[axxxxA-1x_1-5][axxxh-1x_1-4][axxxc-1x_1-5x_4*CO]/1-2-3/a4-b1_a6-c5x");
        //sets.add("WURCS=2.0/2,6,5/[o222h][a222h-1x_1-4]/1-2-2-2-2-2/c1-d1_e1-f1_a?-b1_b?-f?*OPO*/3O/3=O_c?-e?*OPO*/3O/3=O");
        //sets.add("WURCS=2.0/1,3,4/[ha122h-2b_2-5]/1-1-1/a1-b4_a4-c1_b1-c4_c1-c4~n");
        //sets.add("WURCS=2.0/5,11,10/[a2122h-1x_1-5_2*NCC/3=O][a1122h-1x_1-5][a2112h-1x_1-5][Aad21122h-2x_2-6_5*NCC/3=O][Aad21122h-2x_2-6_5*NCCO/3=O]/1-1-2-2-1-3-4-2-1-3-5/a?-b1_b?-c1_c?-d1_c?-h1_d?-e1_e?-f1_f?-g2_h?-i1_i?-j1_j?-k2");
        //sets.add("WURCS=2.0/3,12,16/[a212h-1b_1-5][a2112h-1b_1-5][a211h-1a_1-4]/1-2-2-2-2-2-3-3-2-2-2-2/a4-b1_b3-c1_b6-l1_c3-d1_c6-i1_d3-e1_d6-f1_f6-g1_g3-h1_i3-j1_j3-k1_a1-a4~n_a1-e3~n_b1-b3~n_c1-c3~n_d1-d3~n");
        //sets.add("WURCS=2.0/4,6,6/[a2211m-1a_1-5][a2122A-1b_1-5][a2122h-1b_1-5_3n2-4n1*1OC^RO*2/3CO/6=O/3C][a2112h-1b_1-5]/1-1-1-2-3-4/a2-b1_b3-c1_c2-d1_c3-f1_d4-e1_a1-f4~n");  
        //sets.add("WURCS=2.0/4,6,6/[a2211m-1a_1-5][a2122A-1b_1-5][a2122h-1b_1-5_3n1-4n2*1OC^RO*2/3CO/6=O/3C][a2112h-1b_1-5]/1-1-1-2-3-4/a2-b1_b3-c1_c2-d1_c3-f1_d4-e1_a1-f4~n");
        //sets.add("WURCS=2.0/4,6,6/[a2211m-1a_1-5][a2122A-1b_1-5][a2122h-1b_1-5_3n2-4n1*1OC^RO*2/3CO/6=O/3C][a2112h-1b_1-5]/1-1-1-2-3-4/a2-b1_b3-c1_c2-d1_c3-f1_d4-e1_a1-f4~n");
        //sets.add("WURCS=2.0/2,2,1/[a2112h-1x_1-5_2*NCC/3=O][a21FFA-1a_1-?_2*OSO/3=O/3=O]/1-2/a3-b1");
        //sets.add("WURCS=2.0/4,5,4/[a2122h-1a_1-5_2*N][a1122h-1a_1-5_?*OP^XOCCN/3O/3=O][a2112h-1x_1-5_2*NCC/3=O][a1122h-1a_1-5]/1-2-3-4-2/a4-b1_b6-d1_d2-e1_b?-c1");
        //sets.add("WURCS=2.0/4,5,4/[u2122h_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5]/1-2-3-4-4/a4-b1_b4-c1_c3-d1_c6-e1");
        //sets.add("WURCS=2.0/2,5,5/[a2122h-1a_1-5_2*N][a2121A-1a_1-5]/1-2-1-2-1/a4-b1_b4-c1_c4-d1_d4-e1_b1-c4~n");
        //sets.add("WURCS=2.0/1,7,7/[a2122h-1a_1-5_2*OC_3*OC_6*N]/1-1-1-1-1-1-1/a1-g4_a4-b1_b4-c1_c4-d1_d4-e1_e4-f1_f4-g1");
        //sets.add("WURCS=2.0/1,4,4/[a2122h-1a_1-5]/1-1-1-1/a4-b1_b4-c1_c4-d1_a1-c6");
        //sets.add("WURCS=2.0/4,5,5/[a2112h-1b_1-5_2*NCC/3=O][a2122A-1b_1-5][a2112h-1a_1-5][a2122h-1b_1-5]/1-2-1-3-4/a3-b1_b4-c1_c4-d1_d3-e1_a1-e4~n");
sets.add("WURCS=2.0/4,5,4/[Aad1122h-2x_2-6][a11221h-1x_1-5][a11221h-1x_1-5_3*OP^XOCCN/3O/3=O][a2122h-1x_1-5_2*NCC/3=O]/1-2-3-4-1/a?-b1_a?-e2_b?-c1_c?-d1");

        WURCSImporter wi = new WURCSImporter();
        
        for (String input : sets) {
        	try {
        		wi.start(input);        		
        		ExporterEntrance ee = new ExporterEntrance(wi.getGlyContainer());  	
        		
        		//String greek = ee.toIUPAC(IUPACStyleDescriptor.GREEK);
        		System.out.println(input);
                System.out.println(ee.toIUPAC(IUPACStyleDescriptor.GREEK));
        		System.out.println(ee.toIUPAC(IUPACStyleDescriptor.CONDENSED));
        		System.out.println(ee.toIUPAC(IUPACStyleDescriptor.SHORT));
        		System.out.println("-----------------------------------"); 	
        		//System.out.println(greek);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
    }
}
