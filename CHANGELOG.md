# Change log

## 2.7.0 (Pending)

## 2.6.1 (21210608)
* Added acyl group ()
* Updated [read me](README.md) ()


## 2.6.0 (20210430)
* Removed C-linked substituent from BaseSubstituentTemplate/BaseCrossLinkedTemplate ([#183dc7](https://github.com/glycoinfo/GlycanFormatConverter/commit/183dc76459fa4297e6286624f15fa3ee7f15c49f), [#148bd1](https://github.com/glycoinfo/GlycanFormatConverter/commit/148bd1e6ec4c1096edc26180b361d9c932c70c3c))
  * Updated the substituent analysis process such as MAPAnalyzer
* Source code refactored ([#64f4fd](https://github.com/glycoinfo/GlycanFormatConverter/commit/64f4fd20c6b25e7a58b4f7030daa8e33ea4de935), [#6c34cb](https://github.com/glycoinfo/GlycanFormatConverter/commit/6c34cbfb5a7c42c3a4d720b56108b64a550105f4))
* Modified a converter for KCFToWURCS ([#03ade1](https://github.com/glycoinfo/GlycanFormatConverter/commit/03ade18e8a2c575778fdb2fd27d68af985a90d65))
* Modified a N-linked substituent analysis function for IUPAC exporter ([#808de3](https://github.com/glycoinfo/GlycanFormatConverter/commit/808de3d1508d488e90c0b89ec9ea691d8ea59b22))

## 2.5.3 (20210408)
* Modified IUPAC-Condensed notation parser
* Modified sort function of cyclic for IUPAC-Extended format
* Modified open chain (aldose and ketose) monosaccharide in BackboneToNode
* Fixed typo in validator in IUPAC-Condensed/Extended formats
* Modified head atom checker for substituent in GlyContainer
* Modified KCFToWURCS GlycanFormatConverter ([#d06e5f](https://github.com/glycoinfo/GlycanFormatConverter/commit/d06e5f1fb60039be8e00acabca463d5636ee60a1))

## 2.5.2 (20200730)
* Added validator for exporter ([#540dad](https://github.com/glycoinfo/GlycanFormatConverter/commit/540dadc28ae6f7e652ce3a1bd3771cc3375c7e20), [#cf5029](https://github.com/glycoinfo/GlycanFormatConverter/commit/cf5029a99a09a9b0dab3cf7cae2cad75d34ae48c))
* Refactored source code and modified regex of linkage parser for IUPAC format ([#99d5cf](https://github.com/glycoinfo/GlycanFormatConverter/commit/99d5cf1f7c78b441a607414b05b28924e2d28236))
* Updated WURCSFramework 1.0.0 to 1.0.1 ([#69b7c3](https://github.com/glycoinfo/GlycanFormatConverter/commit/69b7c3627f7e92ee363d3020e89b6ec513cb73d0))

## 2.5.1 (20200730)
* Updated WURCSFramework daily-snapshot to 1.0.0 ([#695b00](https://github.com/glycoinfo/GlycanFormatConverter/commit/695b00beab5e5fe2a62635bfd09f60ca46ab54c1))
* Modified a maven deploy plugin and a maven install plugin ([#cfd91a](https://github.com/glycoinfo/GlycanFormatConverter/commit/cfd91a3553d7a0cfd439ac6dfae427846d7c3301))

## 2.5.0 (20200729)
* 