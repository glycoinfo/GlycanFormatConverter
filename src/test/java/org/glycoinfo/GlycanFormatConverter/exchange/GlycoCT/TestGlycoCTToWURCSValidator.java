package org.glycoinfo.GlycanFormatConverter.exchange.GlycoCT;

public class TestGlycoCTToWURCSValidator {

	public static void main(String[] args) {

		String t_strCSVFile = "./GlyTouCan_sequences_all.csv";

		WURCSToGlycoCTNormalizerFromCSV t_validator = new WURCSToGlycoCTNormalizerFromCSV();
		t_validator.processCSVFile(t_strCSVFile);
	}

}
