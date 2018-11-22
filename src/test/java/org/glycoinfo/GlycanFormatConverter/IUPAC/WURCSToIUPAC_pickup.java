package org.glycoinfo.GlycanFormatConverter.IUPAC;

import org.glycoinfo.GlycanFormatconverter.io.IUPAC.IUPACStyleDescriptor;
import org.glycoinfo.GlycanFormatconverter.io.WURCS.WURCSImporter;
import org.glycoinfo.GlycanFormatconverter.util.ExporterEntrance;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by e15d5605 on 2018/01/24.
 */
public class WURCSToIUPAC_pickup {

    @Test
    public void WURCSToIUPAC () throws Exception {

    //	String numbers = "G00115ZI";
    	
    String numbers = "G09335SZ', 'G12760VJ', 'G34208PJ', 'G34497XL', 'G39177BZ', 'G41586PD', 'G65132JN', 'G67867AY', 'G69666WN', 'G75940FW', 'G83325QY', 'G84356OA', 'G85696MX', 'G87946UV', 'G96377XW', 'G97803IP', 'G98329NH";
    		
    		//"G00185VD', 'G00296FG', 'G00419AT', 'G00544EH', 'G00647EA', 'G00654VN', 'G00889SG', 'G01181OY', 'G01248XR', 'G01717IJ', 'G01749XT', 'G01873WT', 'G01968KY', 'G02001ST', 'G02043VU', 'G02214CC', 'G02328FJ', 'G02352AB', 'G02431QI', 'G02453RE', 'G02464ZS', 'G02603XX', 'G02684PE', 'G03175XQ', 'G03458YJ', 'G03546CI', 'G03607TB', 'G03647LB', 'G03814GO', 'G03891IF', 'G04014CZ', 'G04088WR', 'G04110EV', 'G04141BR', 'G04294GZ', 'G04474WL', 'G04999XI', 'G05139DZ', 'G05215GQ', 'G05285CG', 'G05298YR', 'G05651KT', 'G05892ZF', 'G05896CE', 'G06315TV', 'G06408HR', 'G06640NJ', 'G06799VB', 'G06820CX', 'G07031HL', 'G07075BD', 'G07329IX', 'G07367TV', 'G07580YS', 'G07767AZ', 'G07828TE', 'G08273DI', 'G08518VX', 'G08562VL', 'G08655NA', 'G08658HR', 'G08727OH', 'G09037VU', 'G09322YI', 'G09530UF', 'G09546WM', 'G09977YE', 'G09993QX', 'G10149IV', 'G10364QI', 'G10505YS', 'G10663YM', 'G11296VQ', 'G11313DM', 'G11409BF', 'G11471ZK', 'G11815AV', 'G11847VZ', 'G12237BJ', 'G12505PV', 'G12807TZ', 'G12893PC', 'G12960WM', 'G13228DS', 'G13293HG', 'G13378KM', 'G13464FE', 'G13691VD', 'G13713HM', 'G14014LJ', 'G14146YQ', 'G14340BU', 'G14423UP', 'G14506GF', 'G14516RJ', 'G14562XJ', 'G14786US', 'G15078CF', 'G15651WL', 'G15792IL', 'G16025EW', 'G16234YV', 'G16280YZ', 'G16524OO', 'G16682ID', 'G16810BX', 'G16943VK', 'G17385PU', 'G17730NT', 'G18029AW', 'G18378MW', 'G18415CV', 'G19180VS', 'G19261TG', 'G19270AP', 'G19273ZW', 'G19507SX', 'G19508PX', 'G19565HM', 'G19813TT', 'G19822QK', 'G19849VG', 'G20528KA', 'G20701JH', 'G20944IQ', 'G20960PC', 'G21480FB', 'G21491RV', 'G21565RZ', 'G21603KY', 'G21641EC', 'G21695MX', 'G21751AG', 'G21907WJ', 'G22060OA', 'G22089BU', 'G22199FT', 'G22627HB', 'G23531GF', 'G23599BL', 'G23637XZ', 'G23691LA', 'G23925MM', 'G23983JQ', 'G24012MP', 'G24359ED', 'G25186HB', 'G25541IU', 'G25571WF', 'G25718CN', 'G25744DV', 'G25990FJ', 'G26034LG', 'G26154DS', 'G26368XR', 'G26474ZP', 'G26611MC', 'G26740FE', 'G26833YM', 'G26868TQ', 'G26881CS', 'G26959FY', 'G27347LY', 'G27420BA', 'G27556PD', 'G27745WD', 'G27860TH', 'G28222XB', 'G28479UW', 'G28630QN', 'G28696CE', 'G28789KQ', 'G28937MW', 'G29134PO', 'G29215UY', 'G29493EU', 'G29669DZ', 'G29733BX', 'G29845TV', 'G30298YA', 'G30311TF', 'G30371CC', 'G30503OH', 'G30542YZ', 'G30870QA', 'G31022WC', 'G31121EZ', 'G31206VB', 'G31250JO', 'G31401AV', 'G31824PT', 'G31930AA', 'G32260GV', 'G32325NM', 'G32409AI', 'G32632JB', 'G32684AW', 'G32754KG', 'G33450IH', 'G33692TH', 'G34079UM', 'G34095GC', 'G34618XX', 'G34649RN', 'G34670UJ', 'G34816SZ', 'G34817IR', 'G34903LH', 'G34942PY', 'G34999DS', 'G35012MY', 'G35632QH', 'G36026ZS', 'G36048FJ', 'G36564DT', 'G36565LB', 'G36763WP', 'G36900WZ', 'G37079ML', 'G37317JG', 'G37356JH', 'G37452RG', 'G37535EX', 'G37679HF', 'G37894KK', 'G37899ZI', 'G38217ZV', 'G38294WS', 'G38394RH', 'G38476YV', 'G38488ZU', 'G38905GP', 'G39176VO', 'G39683OR', 'G39928HG', 'G40161DG', 'G40484EA', 'G40509KH', 'G40619ZS', 'G40656EB', 'G40821WT', 'G40832NX', 'G41213VP', 'G41425RV', 'G41599QE', 'G41829XH', 'G41965AK', 'G41999DM', 'G42434HC', 'G43020ZZ', 'G43392WE', 'G43455TT', 'G43779KZ', 'G43825LK', 'G43889BJ', 'G43974XD', 'G44051AM', 'G44059IP', 'G44219NS', 'G44536EQ', 'G44660CC', 'G44877OG', 'G44990FO', 'G45119HS', 'G45160VE', 'G45253PB', 'G45325GK', 'G45358KT', 'G45702JZ', 'G45978SH', 'G46088UT', 'G46314WZ', 'G46585IJ', 'G46656RD', 'G46667UY', 'G46756UD', 'G46957MW', 'G47002RC', 'G47246GC', 'G47498VD', 'G47572NO', 'G47724HT', 'G47816ZB', 'G47839AV', 'G48172IK', 'G48265FX', 'G48271RR', 'G48553NO', 'G48770GQ', 'G48803GF', 'G49011EO', 'G49151RQ', 'G49168WH', 'G49342HF', 'G49353FF', 'G49454UP', 'G49706LP', 'G50173AC', 'G50207OK', 'G50369GI', 'G50387IT', 'G50603ZK', 'G50606TT', 'G50828TB', 'G50874XT', 'G50934HS', 'G51096IJ', 'G51132QS', 'G51410FY', 'G51508QE', 'G51590QX', 'G51676MZ', 'G51952DC', 'G52122IQ', 'G52250VL', 'G52499HS', 'G52717AQ', 'G52822SM', 'G53001YI', 'G53114ZB', 'G53378EO', 'G53972PX', 'G53992OZ', 'G54058SP', 'G54176DA', 'G54329TC', 'G54360RD', 'G54420WC', 'G54442IC', 'G54683IW', 'G54899YM', 'G55043MP', 'G55109ZG', 'G55125MK', 'G55181FR', 'G55235HM', 'G55351CY', 'G55761UL', 'G56233UC', 'G56260US', 'G56551PY', 'G56796AI', 'G56957SM', 'G57099RR', 'G57481VS', 'G57692BG', 'G57700ML', 'G57910QA', 'G57971HQ', 'G58136NZ', 'G58159YE', 'G58261JL', 'G58318YB', 'G58508OR', 'G58803ER', 'G58838FK', 'G58983DB', 'G59090TN', 'G59152IH', 'G59170SC', 'G59551JV', 'G59639GZ', 'G59668ZD', 'G59936SM', 'G60561GL', 'G60602PI', 'G60766VE', 'G60868XE', 'G60875EM', 'G61111MU', 'G61126ED', 'G61984XW', 'G62061OG', 'G62335LM', 'G62660UJ', 'G62676XB', 'G62688QC', 'G62799AQ', 'G62816HE', 'G62944IK', 'G63121XO', 'G63574EM', 'G64503CB', 'G64532NY', 'G64647YO', 'G64810EN', 'G64871OT', 'G64908FQ', 'G64913MX', 'G65276UR', 'G65374YF', 'G65570RE', 'G65606ZS', 'G65949JJ', 'G66011SA', 'G66622EU', 'G66633LB', 'G66845IV', 'G67105GF', 'G67177MG', 'G67558UG', 'G67575VU', 'G67886PS', 'G68257OW', 'G68434WI', 'G68666JO', 'G68770GR', 'G68959GB', 'G69418VR', 'G69547UE', 'G69767CQ', 'G70021LC', 'G70221XJ', 'G70576HK', 'G70649CI', 'G70977RA', 'G71084RO', 'G71673RO', 'G71886NH', 'G71952HE', 'G72312XO', 'G72366JK', 'G72623KS', 'G72742AF', 'G72779TF', 'G72906YR', 'G73214HU', 'G73415MF', 'G73446VE', 'G73525AZ', 'G73680FH', 'G73730YY', 'G73892PO', 'G73938HL', 'G74272OM', 'G74444DB', 'G74806KL', 'G74905VN', 'G75215RK', 'G75262XY', 'G75435TW', 'G75458TV', 'G75476RD', 'G75565HZ', 'G75669MV', 'G75933MN', 'G75993SS', 'G75999BZ', 'G76216QA', 'G76455IT', 'G76835FA', 'G76951PF', 'G76957ZT', 'G77350PT', 'G77370AA', 'G77384SD', 'G77496BX', 'G77851MI', 'G78039HQ', 'G78079ES', 'G78142NX', 'G78254YQ', 'G78275JH', 'G78397TT', 'G78653QU', 'G78749LG', 'G78787DV', 'G78832YY', 'G78871YO', 'G79091GX', 'G79494IJ', 'G79615UL', 'G79850ME', 'G79904XH', 'G80007QI', 'G80179HE', 'G80205KE', 'G80411BL', 'G80466UQ', 'G80475ZA', 'G80676MU', 'G80949EA', 'G80985QV', 'G80992YA', 'G81176SU', 'G81267MJ', 'G81276ZO', 'G81411TM', 'G81640ZP', 'G81860CD', 'G81932BQ', 'G81945CM', 'G82022XN', 'G82179TV', 'G82201JW', 'G82429XV', 'G82615DU', 'G82652IN', 'G82931VH', 'G82941KB', 'G83106VW', 'G83176EV', 'G83327MW', 'G83397KM', 'G83439GF', 'G83567DT', 'G83879CF', 'G84587LD', 'G85309WS', 'G85534QX', 'G85812LL', 'G85945HT', 'G86153KA', 'G86248XE', 'G86445CU', 'G86525OW', 'G86526UX', 'G86554JP', 'G86629WR', 'G86699GL', 'G87021RO', 'G87257XF', 'G87391SF', 'G87480OF', 'G87481LC', 'G87562HM', 'G87625OX', 'G87887CA', 'G87993NJ', 'G88049PO', 'G88110YN', 'G88325SR', 'G88339KJ', 'G88376JG', 'G88622IU', 'G88649MQ', 'G88778DU', 'G88845GF', 'G88912NB', 'G89034OF', 'G89072IF', 'G89225JU', 'G89407LO', 'G89631VC', 'G89637DY', 'G89755ME', 'G89767LB', 'G89922XT', 'G90196RB', 'G90208WQ', 'G90268HK', 'G90651QS', 'G90709SK', 'G90719KG', 'G91144JG', 'G91407PO', 'G91484AA', 'G91520YY', 'G91622EF', 'G91662AB', 'G91677JR', 'G91721RO', 'G91722KR', 'G91753ZV', 'G91845ES', 'G92068NP', 'G92242OY', 'G92381JX', 'G92389SI', 'G92446EV', 'G92553UY', 'G92638HO', 'G92656JT', 'G93477BL', 'G93501FL', 'G93687TJ', 'G93722KI', 'G93758FV', 'G94066CA', 'G94194UN', 'G94199XU', 'G94349EL', 'G94647ZA', 'G95019OH', 'G95272JN', 'G95863VE', 'G95941LA', 'G95974WK', 'G96454JH', 'G96568NT', 'G96845TR', 'G96896OC', 'G97015BO', 'G97080BE', 'G97383DT', 'G97590CS', 'G97640US', 'G97640WY', 'G97852MP', 'G98272YB', 'G98328XH', 'G99407GK', 'G99732SO', 'G99771WN";
    		
    	//"G57657JA";
    
    //"G64296EN";//"G00048ZA";//"G00301XV";
    	
    	//	String numbers = "G74678YC', 'G95576XZ', 'G67495JG', 'G36327IZ', 'G94304ZN";
    	
    	//String numbers = "G12796TN', 'G49461WX', 'G62437SF', 'G73501HK', 'G74878EF', 'G81642MI', 'G38352BU";
    	
    	//<Q>
    	//String numbers = "G00301XV', 'G00472TL', 'G01819ZN', 'G01954GZ', 'G03015AX', 'G03690AY', 'G04785ZH', 'G06932JZ', 'G07174KI', 'G08553IB', 'G09689DR', 'G10534JP', 'G12851XW', 'G13709MU', 'G13807HJ', 'G15678FJ', 'G16558PB', 'G17738UB', 'G17888GZ', 'G20292CA', 'G22379AK', 'G24566JM', 'G24794OG', 'G24830SA', 'G25220NO', 'G26142PM', 'G27081WU', 'G28428NG', 'G29992EE', 'G30889OL', 'G33949FE', 'G35455XI', 'G36400XY', 'G36647EN', 'G36672DN', 'G37201NW', 'G37238PW', 'G37589TD', 'G37687OM', 'G38925PI', 'G39386NM', 'G40236EN', 'G43203OV', 'G43962PZ', 'G44387PI', 'G46216PH', 'G47076CS', 'G47257BG', 'G47734IW', 'G49291NZ', 'G52226HR', 'G52912WO', 'G53621MK', 'G58756VI', 'G61051JB', 'G61365VY', 'G61390LD', 'G63423BO', 'G64964HW', 'G65131ME', 'G65448KP', 'G65726MC', 'G66745OK', 'G66858QT', 'G66957SK', 'G67009FG', 'G68714YC', 'G70823BY', 'G72456QT', 'G72851VK', 'G73245UM', 'G73785CC', 'G77496LB', 'G78905YB', 'G82955BL', 'G84510DI', 'G85692VJ', 'G86619JX', 'G86638DP', 'G88696LA', 'G91037SI', 'G91192KL', 'G91369NN', 'G92435YW', 'G94434RV', 'G96428KL', 'G96952QM', 'G98115NV', 'G99628DW', 'G99990RL";
    	File file = new File("/Users/e15d5605/Dataset/sampleWURCSforConvertTest");

    	if (file.isFile()) {
    		HashMap<String, String> wurcsMap = openString(file.getAbsolutePath());

    		StringBuilder result = new StringBuilder();
    		WURCSImporter wi = new WURCSImporter();
    		
    		ArrayList<String> codes = new ArrayList<String>();
    		
    		for (String number : numbers.split("', '")) {
    			String wurcs = wurcsMap.get(number);
    			
    			try {
    				/* WURCS to IUPAC */	
    				wi.start(wurcs);

    				ExporterEntrance ee = new ExporterEntrance(wi.getGlyContainer());
    				String iupac = ee.toIUPAC(IUPACStyleDescriptor.GREEK);
                //result.append(number + " " + iupac + "\n");
                result.append(wurcs + "\n");   
                //System.out.println(ee.toIUPAC(IUPACStyleDescriptor.EXTENDED));
                //System.out.println(ee.toIUPAC(IUPACStyleDescriptor.CONDENSED));
                //System.out.println(ee.toIUPAC(IUPACStyleDescriptor.SHORT));
    			} catch (Exception e) {
    				String message = e.getMessage();
    				message = message.replace(" could not handled", "");
    				codes.add(message);
    				//result.append(e.getMessage() + "\n");
    				//e.printStackTrace();
    			}
    		}
    		
    		HashMap<Integer, Integer> count = new HashMap<Integer, Integer>();
    		for (String code : codes) {
    			if (!count.containsKey(code.length())) {
    				count.put(code.length(), 1);
    			} else {
    				int c = count.get(code.length());
    				c++;
    				count.put(code.length(), c);
    			}
    		}
    		
    		System.out.println(count);
    		
    		System.out.println(result);
    		
    	} else {
    		throw new Exception("This file could not found.");
    	}
    }

    /**
     *
     * @param a_strFile
     * @return
     * @throws Exception
     */
    private HashMap<String, String> openString(String a_strFile) throws Exception {
        try {
            return readWURCS(new BufferedReader(new FileReader(a_strFile)));
        }catch (IOException e) {
            throw new Exception();
        }
    }

    /**
     *
     * @param a_bfFile
     * @return
     * @throws IOException
     */
    private HashMap<String, String> readWURCS(BufferedReader a_bfFile) throws IOException {
        String line = "";
        HashMap<String, String> wret = new HashMap<String, String>();
        wret.clear();

        while((line = a_bfFile.readLine()) != null) {
            line.trim();
            if(line.indexOf("WURCS") != -1) {
                if(line.indexOf(" ") != -1) line = line.replace(" ", "\t");
                String[] IDandWURCS = line.split("\t");
                if (IDandWURCS.length == 2) {
                    wret.put(IDandWURCS[0].trim(), IDandWURCS[1]);
                }
            }
        }
        a_bfFile.close();

        return wret;
    }
}
