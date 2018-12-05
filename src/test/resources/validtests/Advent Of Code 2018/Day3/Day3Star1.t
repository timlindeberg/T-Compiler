import T::std::HashMap
import java::util::regex::Matcher
import java::util::regex::Pattern

val input =`#1 @ 236,827: 24x17
#2 @ 963,165: 29x21
#3 @ 574,56: 23x28
#4 @ 358,362: 26x25
#5 @ 740,379: 22x25
#6 @ 178,647: 8x8
#7 @ 699,379: 10x27
#8 @ 456,958: 11x20
#9 @ 824,624: 20x20
#10 @ 322,450: 14x11
#11 @ 311,5: 27x12
#12 @ 512,893: 16x22
#13 @ 293,892: 11x13
#14 @ 51,167: 29x19
#15 @ 372,778: 14x19
#16 @ 682,523: 16x21
#17 @ 681,896: 15x22
#18 @ 545,802: 14x18
#19 @ 99,208: 11x16
#20 @ 596,47: 26x18
#21 @ 258,360: 24x28
#22 @ 708,321: 29x29
#23 @ 779,401: 10x21
#24 @ 101,861: 20x16
#25 @ 794,513: 10x27
#26 @ 616,535: 17x16
#27 @ 355,118: 12x15
#28 @ 950,197: 27x29
#29 @ 612,9: 14x22
#30 @ 230,473: 10x27
#31 @ 348,233: 12x13
#32 @ 359,160: 22x15
#33 @ 568,560: 26x18
#34 @ 433,249: 10x25
#35 @ 531,941: 15x25
#36 @ 383,971: 19x11
#37 @ 837,321: 23x12
#38 @ 433,6: 29x22
#39 @ 616,14: 15x25
#40 @ 653,897: 10x28
#41 @ 875,475: 25x20
#42 @ 500,727: 27x23
#43 @ 72,525: 16x13
#44 @ 253,10: 18x16
#45 @ 430,25: 19x16
#46 @ 681,383: 28x25
#47 @ 99,785: 15x28
#48 @ 852,437: 27x16
#49 @ 535,208: 17x21
#50 @ 789,573: 26x25
#51 @ 46,913: 20x12
#52 @ 334,924: 13x14
#53 @ 952,28: 22x24
#54 @ 271,830: 20x23
#55 @ 441,752: 14x18
#56 @ 670,55: 19x13
#57 @ 217,179: 11x11
#58 @ 651,640: 20x28
#59 @ 83,258: 26x24
#60 @ 53,827: 12x17
#61 @ 60,680: 15x20
#62 @ 613,573: 16x27
#63 @ 176,263: 23x29
#64 @ 534,921: 28x27
#65 @ 869,450: 22x23
#66 @ 501,357: 16x14
#67 @ 460,656: 22x23
#68 @ 488,785: 25x19
#69 @ 321,792: 19x13
#70 @ 439,319: 20x13
#71 @ 358,638: 14x24
#72 @ 674,246: 17x23
#73 @ 61,230: 10x15
#74 @ 586,961: 29x25
#75 @ 270,758: 24x21
#76 @ 483,164: 25x19
#77 @ 831,622: 12x10
#78 @ 902,614: 23x23
#79 @ 119,390: 27x17
#80 @ 179,180: 12x18
#81 @ 188,641: 14x23
#82 @ 387,527: 16x28
#83 @ 882,648: 23x24
#84 @ 865,98: 29x13
#85 @ 497,829: 23x21
#86 @ 418,26: 14x20
#87 @ 685,721: 14x16
#88 @ 682,917: 11x13
#89 @ 618,649: 16x17
#90 @ 31,781: 27x26
#91 @ 896,480: 12x27
#92 @ 156,318: 14x20
#93 @ 382,609: 22x28
#94 @ 621,526: 19x27
#95 @ 148,143: 14x22
#96 @ 511,640: 29x10
#97 @ 847,879: 27x12
#98 @ 460,449: 26x28
#99 @ 688,107: 15x20
#100 @ 588,484: 24x19
#101 @ 538,816: 19x17
#102 @ 214,685: 25x18
#103 @ 159,486: 24x19
#104 @ 868,580: 10x26
#105 @ 919,133: 11x10
#106 @ 836,646: 18x14
#107 @ 681,734: 13x15
#108 @ 579,433: 20x22
#109 @ 146,579: 29x29
#110 @ 639,104: 11x20
#111 @ 656,269: 20x21
#112 @ 675,649: 21x13
#113 @ 933,213: 18x11
#114 @ 526,598: 16x10
#115 @ 880,657: 22x28
#116 @ 188,741: 3x8
#117 @ 529,101: 20x22
#118 @ 831,141: 12x11
#119 @ 236,736: 15x12
#120 @ 477,180: 16x25
#121 @ 159,468: 10x20
#122 @ 154,430: 24x24
#123 @ 8,416: 26x26
#124 @ 629,566: 12x13
#125 @ 6,609: 14x18
#126 @ 112,519: 16x11
#127 @ 534,189: 23x21
#128 @ 744,719: 11x19
#129 @ 585,952: 16x12
#130 @ 208,120: 12x16
#131 @ 366,301: 19x12
#132 @ 385,726: 21x29
#133 @ 197,652: 29x24
#134 @ 169,777: 18x28
#135 @ 629,819: 10x26
#136 @ 59,693: 26x11
#137 @ 846,111: 28x11
#138 @ 178,397: 24x18
#139 @ 73,369: 20x17
#140 @ 652,865: 16x26
#141 @ 369,349: 28x21
#142 @ 495,964: 11x24
#143 @ 219,40: 28x20
#144 @ 915,463: 17x29
#145 @ 478,630: 20x26
#146 @ 193,759: 21x19
#147 @ 917,810: 23x13
#148 @ 696,7: 23x13
#149 @ 274,721: 20x20
#150 @ 606,331: 15x15
#151 @ 274,790: 20x29
#152 @ 722,171: 15x23
#153 @ 952,615: 27x27
#154 @ 712,18: 19x13
#155 @ 482,501: 20x19
#156 @ 164,672: 25x27
#157 @ 315,742: 23x24
#158 @ 950,140: 20x11
#159 @ 561,981: 24x13
#160 @ 267,280: 17x28
#161 @ 595,861: 27x12
#162 @ 229,563: 27x29
#163 @ 842,82: 29x26
#164 @ 442,474: 15x19
#165 @ 675,726: 18x18
#166 @ 409,483: 16x18
#167 @ 339,116: 15x28
#168 @ 316,520: 17x12
#169 @ 833,146: 10x18
#170 @ 598,50: 21x11
#171 @ 559,340: 29x28
#172 @ 389,109: 17x25
#173 @ 575,195: 29x15
#174 @ 625,974: 10x24
#175 @ 586,285: 16x23
#176 @ 679,337: 15x26
#177 @ 452,174: 19x24
#178 @ 104,187: 16x14
#179 @ 331,334: 10x29
#180 @ 627,977: 24x12
#181 @ 644,74: 23x18
#182 @ 810,416: 15x19
#183 @ 375,495: 28x10
#184 @ 702,898: 10x27
#185 @ 437,76: 17x27
#186 @ 612,797: 23x29
#187 @ 1,171: 20x11
#188 @ 55,417: 24x21
#189 @ 81,211: 17x19
#190 @ 520,78: 25x27
#191 @ 224,906: 13x10
#192 @ 734,526: 17x19
#193 @ 351,159: 29x15
#194 @ 364,26: 16x20
#195 @ 113,279: 20x24
#196 @ 480,276: 24x22
#197 @ 602,306: 25x29
#198 @ 370,361: 24x21
#199 @ 54,492: 23x18
#200 @ 838,649: 11x3
#201 @ 65,143: 21x13
#202 @ 150,606: 21x12
#203 @ 52,202: 24x11
#204 @ 38,533: 13x17
#205 @ 955,722: 24x18
#206 @ 835,200: 11x10
#207 @ 962,816: 15x25
#208 @ 64,155: 21x29
#209 @ 381,759: 21x20
#210 @ 112,225: 19x16
#211 @ 183,93: 17x18
#212 @ 120,131: 9x4
#213 @ 323,749: 12x26
#214 @ 726,270: 28x12
#215 @ 398,503: 20x17
#216 @ 445,658: 18x22
#217 @ 800,606: 11x17
#218 @ 238,606: 18x12
#219 @ 143,242: 25x18
#220 @ 155,471: 25x11
#221 @ 352,114: 21x22
#222 @ 684,785: 19x17
#223 @ 185,651: 19x22
#224 @ 585,323: 15x24
#225 @ 783,566: 29x13
#226 @ 691,242: 26x12
#227 @ 960,178: 12x15
#228 @ 22,347: 18x17
#229 @ 852,794: 10x19
#230 @ 802,832: 11x10
#231 @ 396,661: 23x24
#232 @ 804,424: 29x25
#233 @ 69,511: 26x16
#234 @ 427,894: 23x29
#235 @ 46,646: 24x11
#236 @ 902,710: 23x29
#237 @ 323,12: 29x12
#238 @ 974,277: 22x16
#239 @ 42,728: 29x13
#240 @ 131,366: 21x14
#241 @ 843,490: 12x28
#242 @ 887,260: 27x13
#243 @ 34,628: 18x22
#244 @ 564,641: 22x27
#245 @ 112,102: 12x13
#246 @ 577,984: 10x12
#247 @ 429,382: 23x20
#248 @ 82,152: 16x15
#249 @ 968,821: 21x12
#250 @ 599,766: 11x11
#251 @ 738,397: 19x11
#252 @ 188,772: 21x12
#253 @ 128,358: 16x20
#254 @ 324,334: 26x27
#255 @ 620,165: 28x16
#256 @ 964,721: 29x23
#257 @ 663,297: 27x28
#258 @ 810,836: 18x28
#259 @ 966,710: 11x19
#260 @ 24,909: 18x25
#261 @ 887,815: 29x13
#262 @ 768,639: 29x15
#263 @ 231,260: 13x21
#264 @ 609,498: 25x15
#265 @ 648,893: 23x26
#266 @ 505,183: 26x10
#267 @ 659,49: 12x16
#268 @ 399,838: 23x16
#269 @ 894,238: 18x11
#270 @ 263,722: 29x22
#271 @ 6,451: 16x28
#272 @ 519,58: 22x20
#273 @ 798,32: 24x14
#274 @ 563,444: 22x24
#275 @ 305,394: 27x13
#276 @ 71,668: 28x23
#277 @ 54,104: 25x20
#278 @ 637,68: 25x27
#279 @ 369,584: 21x29
#280 @ 744,789: 28x23
#281 @ 654,942: 27x19
#282 @ 299,94: 14x13
#283 @ 446,431: 28x23
#284 @ 565,629: 21x27
#285 @ 663,944: 7x12
#286 @ 0,295: 26x14
#287 @ 164,639: 10x27
#288 @ 683,289: 13x19
#289 @ 58,724: 28x26
#290 @ 22,57: 20x17
#291 @ 575,396: 13x18
#292 @ 753,890: 18x25
#293 @ 364,761: 24x12
#294 @ 924,561: 22x25
#295 @ 195,512: 25x23
#296 @ 548,269: 22x27
#297 @ 503,126: 23x11
#298 @ 777,299: 19x23
#299 @ 702,883: 20x24
#300 @ 381,580: 17x19
#301 @ 441,913: 28x14
#302 @ 669,100: 25x25
#303 @ 32,197: 13x18
#304 @ 265,839: 25x29
#305 @ 919,690: 11x12
#306 @ 63,412: 11x15
#307 @ 793,239: 14x25
#308 @ 503,967: 15x27
#309 @ 372,595: 15x26
#310 @ 406,102: 17x10
#311 @ 451,923: 29x19
#312 @ 322,456: 28x14
#313 @ 518,458: 19x21
#314 @ 802,490: 27x17
#315 @ 275,275: 22x16
#316 @ 542,235: 20x25
#317 @ 206,443: 10x24
#318 @ 98,456: 18x21
#319 @ 14,177: 20x24
#320 @ 470,927: 27x20
#321 @ 236,732: 28x15
#322 @ 978,392: 14x11
#323 @ 17,410: 11x13
#324 @ 419,5: 28x11
#325 @ 479,860: 19x28
#326 @ 478,401: 23x26
#327 @ 936,903: 11x12
#328 @ 420,400: 17x17
#329 @ 358,621: 23x17
#330 @ 472,16: 14x17
#331 @ 883,26: 10x11
#332 @ 867,479: 25x10
#333 @ 380,69: 22x16
#334 @ 57,678: 23x17
#335 @ 448,955: 14x14
#336 @ 470,303: 25x12
#337 @ 378,240: 27x26
#338 @ 599,567: 3x7
#339 @ 298,489: 28x28
#340 @ 748,88: 17x16
#341 @ 294,122: 17x15
#342 @ 351,17: 15x17
#343 @ 73,352: 22x15
#344 @ 169,694: 19x15
#345 @ 221,181: 15x19
#346 @ 3,442: 20x15
#347 @ 103,801: 27x14
#348 @ 630,65: 22x29
#349 @ 544,377: 24x25
#350 @ 652,162: 15x29
#351 @ 933,286: 25x28
#352 @ 475,900: 26x12
#353 @ 59,542: 10x8
#354 @ 900,628: 18x29
#355 @ 20,693: 15x11
#356 @ 64,709: 15x14
#357 @ 115,525: 13x21
#358 @ 673,940: 25x14
#359 @ 324,57: 25x26
#360 @ 659,83: 18x16
#361 @ 580,336: 18x10
#362 @ 108,196: 14x11
#363 @ 410,669: 12x19
#364 @ 172,156: 25x18
#365 @ 459,560: 11x26
#366 @ 304,477: 11x14
#367 @ 698,700: 13x16
#368 @ 67,219: 10x22
#369 @ 132,404: 23x28
#370 @ 156,868: 10x15
#371 @ 14,386: 28x12
#372 @ 161,828: 19x21
#373 @ 129,136: 28x11
#374 @ 564,536: 21x16
#375 @ 163,305: 23x16
#376 @ 647,578: 29x19
#377 @ 92,636: 22x13
#378 @ 671,634: 23x24
#379 @ 62,150: 15x23
#380 @ 346,745: 26x23
#381 @ 512,359: 10x17
#382 @ 591,462: 24x28
#383 @ 363,302: 28x17
#384 @ 606,253: 26x22
#385 @ 717,664: 24x13
#386 @ 199,503: 13x21
#387 @ 351,733: 20x15
#388 @ 903,408: 14x18
#389 @ 716,635: 14x18
#390 @ 359,301: 13x13
#391 @ 503,813: 12x12
#392 @ 625,197: 19x21
#393 @ 0,875: 29x13
#394 @ 852,481: 14x10
#395 @ 404,556: 17x4
#396 @ 45,545: 24x11
#397 @ 131,525: 10x14
#398 @ 14,445: 15x26
#399 @ 162,304: 25x22
#400 @ 485,283: 25x27
#401 @ 840,449: 27x22
#402 @ 245,413: 17x19
#403 @ 356,321: 28x13
#404 @ 935,270: 16x26
#405 @ 218,148: 14x24
#406 @ 171,635: 14x10
#407 @ 342,701: 16x19
#408 @ 85,191: 21x22
#409 @ 440,70: 16x24
#410 @ 974,182: 22x15
#411 @ 835,499: 19x12
#412 @ 597,704: 17x12
#413 @ 139,275: 29x21
#414 @ 168,128: 17x22
#415 @ 370,35: 16x28
#416 @ 245,552: 10x12
#417 @ 296,693: 15x20
#418 @ 801,824: 25x21
#419 @ 88,324: 11x28
#420 @ 953,243: 21x22
#421 @ 325,800: 24x12
#422 @ 909,963: 13x25
#423 @ 537,283: 18x16
#424 @ 551,903: 14x11
#425 @ 219,737: 28x16
#426 @ 167,856: 14x17
#427 @ 890,91: 21x24
#428 @ 283,590: 21x21
#429 @ 698,549: 12x14
#430 @ 139,144: 29x12
#431 @ 438,154: 19x17
#432 @ 820,251: 26x15
#433 @ 365,978: 13x18
#434 @ 443,491: 15x25
#435 @ 362,975: 29x23
#436 @ 259,768: 25x18
#437 @ 154,886: 11x13
#438 @ 117,775: 21x28
#439 @ 388,232: 11x12
#440 @ 472,122: 26x28
#441 @ 177,255: 18x20
#442 @ 840,473: 22x27
#443 @ 961,250: 14x29
#444 @ 864,424: 13x27
#445 @ 902,854: 11x12
#446 @ 291,431: 20x29
#447 @ 227,737: 25x20
#448 @ 960,385: 25x24
#449 @ 102,719: 24x18
#450 @ 584,813: 10x20
#451 @ 139,154: 24x26
#452 @ 813,367: 24x27
#453 @ 485,255: 15x22
#454 @ 618,938: 21x29
#455 @ 396,87: 15x15
#456 @ 826,806: 20x20
#457 @ 685,868: 26x24
#458 @ 934,664: 19x15
#459 @ 268,224: 28x25
#460 @ 759,677: 15x23
#461 @ 547,679: 18x10
#462 @ 104,374: 29x11
#463 @ 638,492: 11x28
#464 @ 767,805: 14x15
#465 @ 655,957: 21x24
#466 @ 540,60: 17x27
#467 @ 775,787: 18x28
#468 @ 211,389: 12x11
#469 @ 213,97: 21x25
#470 @ 485,438: 24x19
#471 @ 466,191: 22x18
#472 @ 140,168: 19x23
#473 @ 747,632: 29x24
#474 @ 463,180: 24x27
#475 @ 567,631: 14x12
#476 @ 127,139: 14x14
#477 @ 347,1: 21x25
#478 @ 132,360: 16x26
#479 @ 226,938: 26x12
#480 @ 664,209: 11x27
#481 @ 619,206: 23x27
#482 @ 80,605: 12x22
#483 @ 465,171: 22x22
#484 @ 166,120: 22x19
#485 @ 442,763: 24x18
#486 @ 641,256: 10x29
#487 @ 521,55: 22x22
#488 @ 316,285: 24x27
#489 @ 591,765: 23x18
#490 @ 699,58: 15x17
#491 @ 234,986: 10x10
#492 @ 7,644: 12x25
#493 @ 640,268: 27x27
#494 @ 494,491: 27x17
#495 @ 785,569: 16x28
#496 @ 204,372: 13x24
#497 @ 276,600: 20x17
#498 @ 655,666: 16x12
#499 @ 946,481: 29x15
#500 @ 196,734: 26x20
#501 @ 428,471: 15x12
#502 @ 635,381: 28x21
#503 @ 638,970: 21x15
#504 @ 40,692: 22x27
#505 @ 753,278: 13x20
#506 @ 269,647: 19x14
#507 @ 838,18: 15x25
#508 @ 273,727: 10x23
#509 @ 447,111: 19x13
#510 @ 771,820: 10x10
#511 @ 239,33: 13x27
#512 @ 871,903: 15x28
#513 @ 171,645: 24x13
#514 @ 864,892: 21x15
#515 @ 427,378: 24x10
#516 @ 688,637: 17x14
#517 @ 66,674: 14x29
#518 @ 962,89: 21x21
#519 @ 778,184: 19x13
#520 @ 375,275: 12x29
#521 @ 48,414: 13x10
#522 @ 455,904: 15x20
#523 @ 102,346: 10x10
#524 @ 406,533: 13x21
#525 @ 349,708: 25x28
#526 @ 388,879: 26x26
#527 @ 845,882: 26x10
#528 @ 345,519: 20x21
#529 @ 56,382: 27x20
#530 @ 902,93: 25x16
#531 @ 74,207: 15x18
#532 @ 138,590: 11x10
#533 @ 402,554: 24x10
#534 @ 595,347: 18x13
#535 @ 103,910: 27x26
#536 @ 274,316: 20x20
#537 @ 256,301: 29x24
#538 @ 1,311: 20x25
#539 @ 392,42: 25x26
#540 @ 389,660: 16x14
#541 @ 444,669: 17x19
#542 @ 558,227: 23x16
#543 @ 881,869: 10x26
#544 @ 799,601: 29x15
#545 @ 722,757: 14x21
#546 @ 444,156: 7x6
#547 @ 723,61: 13x10
#548 @ 920,327: 26x12
#549 @ 236,460: 26x24
#550 @ 352,125: 23x25
#551 @ 672,250: 26x10
#552 @ 592,981: 29x18
#553 @ 166,330: 16x20
#554 @ 513,881: 26x22
#555 @ 621,875: 27x15
#556 @ 42,361: 23x27
#557 @ 564,567: 17x13
#558 @ 949,10: 25x25
#559 @ 179,860: 11x20
#560 @ 714,180: 13x12
#561 @ 834,556: 11x11
#562 @ 547,289: 13x14
#563 @ 11,289: 19x22
#564 @ 751,848: 10x27
#565 @ 36,229: 16x17
#566 @ 425,484: 26x18
#567 @ 596,333: 19x29
#568 @ 193,300: 23x23
#569 @ 344,497: 23x27
#570 @ 795,840: 15x15
#571 @ 152,832: 25x24
#572 @ 380,575: 16x18
#573 @ 271,817: 25x25
#574 @ 850,120: 19x16
#575 @ 442,62: 26x28
#576 @ 469,879: 14x23
#577 @ 175,437: 11x22
#578 @ 977,171: 18x23
#579 @ 472,357: 16x25
#580 @ 881,776: 24x13
#581 @ 549,223: 17x13
#582 @ 603,488: 13x13
#583 @ 14,209: 25x10
#584 @ 196,379: 13x20
#585 @ 358,174: 22x26
#586 @ 919,678: 19x29
#587 @ 597,564: 13x23
#588 @ 373,674: 23x29
#589 @ 644,89: 22x22
#590 @ 26,918: 28x13
#591 @ 13,151: 18x10
#592 @ 264,253: 8x4
#593 @ 436,290: 16x23
#594 @ 766,181: 16x14
#595 @ 93,327: 17x10
#596 @ 439,117: 17x13
#597 @ 806,585: 18x19
#598 @ 102,96: 15x29
#599 @ 18,622: 27x27
#600 @ 482,554: 22x20
#601 @ 836,588: 9x6
#602 @ 799,250: 24x17
#603 @ 604,642: 19x28
#604 @ 812,638: 21x27
#605 @ 880,168: 24x21
#606 @ 441,321: 15x8
#607 @ 796,536: 23x15
#608 @ 872,485: 29x13
#609 @ 350,214: 28x22
#610 @ 52,397: 27x23
#611 @ 937,794: 20x18
#612 @ 689,735: 16x12
#613 @ 311,768: 14x12
#614 @ 536,862: 28x28
#615 @ 576,952: 24x14
#616 @ 108,343: 10x23
#617 @ 398,229: 17x18
#618 @ 433,940: 21x21
#619 @ 368,757: 25x22
#620 @ 690,637: 13x17
#621 @ 137,142: 15x28
#622 @ 26,191: 24x24
#623 @ 57,668: 23x17
#624 @ 492,513: 18x17
#625 @ 110,341: 16x21
#626 @ 596,557: 15x17
#627 @ 699,558: 13x26
#628 @ 548,643: 20x14
#629 @ 970,474: 22x29
#630 @ 365,772: 13x16
#631 @ 382,845: 27x15
#632 @ 392,105: 29x23
#633 @ 725,512: 16x19
#634 @ 180,715: 24x13
#635 @ 402,623: 12x21
#636 @ 880,168: 25x15
#637 @ 19,648: 24x26
#638 @ 784,405: 12x29
#639 @ 104,500: 13x25
#640 @ 625,898: 10x22
#641 @ 675,252: 27x19
#642 @ 288,838: 19x21
#643 @ 677,259: 22x14
#644 @ 17,657: 27x25
#645 @ 883,476: 24x24
#646 @ 700,472: 27x15
#647 @ 836,523: 24x28
#648 @ 535,413: 12x28
#649 @ 882,809: 22x16
#650 @ 768,913: 18x19
#651 @ 821,194: 25x10
#652 @ 668,654: 17x18
#653 @ 174,874: 27x11
#654 @ 504,721: 22x22
#655 @ 391,76: 20x23
#656 @ 674,365: 11x27
#657 @ 165,798: 12x26
#658 @ 773,237: 16x27
#659 @ 587,487: 16x27
#660 @ 344,802: 28x17
#661 @ 344,354: 20x25
#662 @ 485,206: 18x12
#663 @ 834,583: 14x17
#664 @ 506,585: 16x20
#665 @ 256,215: 19x12
#666 @ 287,74: 17x23
#667 @ 361,311: 19x17
#668 @ 651,419: 25x14
#669 @ 359,762: 28x29
#670 @ 452,936: 23x24
#671 @ 70,637: 13x22
#672 @ 324,0: 19x10
#673 @ 69,607: 20x22
#674 @ 35,901: 22x24
#675 @ 559,875: 18x25
#676 @ 343,629: 18x13
#677 @ 436,947: 14x3
#678 @ 967,13: 28x16
#679 @ 526,802: 21x23
#680 @ 669,950: 10x13
#681 @ 823,792: 12x25
#682 @ 439,901: 29x20
#683 @ 103,931: 23x15
#684 @ 815,499: 12x14
#685 @ 383,654: 23x14
#686 @ 694,72: 25x18
#687 @ 661,50: 28x12
#688 @ 104,197: 22x10
#689 @ 400,12: 20x12
#690 @ 46,225: 12x11
#691 @ 444,426: 13x13
#692 @ 906,595: 14x17
#693 @ 978,625: 16x21
#694 @ 812,350: 12x22
#695 @ 560,549: 29x15
#696 @ 531,593: 20x16
#697 @ 187,511: 25x16
#698 @ 857,953: 12x21
#699 @ 292,80: 16x18
#700 @ 72,448: 11x28
#701 @ 351,633: 19x10
#702 @ 167,91: 21x14
#703 @ 41,377: 13x19
#704 @ 580,562: 20x26
#705 @ 725,51: 11x27
#706 @ 64,422: 17x27
#707 @ 529,173: 13x13
#708 @ 446,320: 26x21
#709 @ 182,501: 24x19
#710 @ 435,920: 16x16
#711 @ 575,395: 27x17
#712 @ 611,796: 11x18
#713 @ 669,610: 25x23
#714 @ 900,852: 18x23
#715 @ 772,807: 20x10
#716 @ 839,514: 14x11
#717 @ 966,184: 20x29
#718 @ 352,126: 12x25
#719 @ 98,259: 11x26
#720 @ 911,486: 22x24
#721 @ 758,91: 29x20
#722 @ 859,961: 11x28
#723 @ 563,236: 20x13
#724 @ 456,649: 17x15
#725 @ 270,10: 25x11
#726 @ 522,399: 18x20
#727 @ 483,122: 28x23
#728 @ 587,313: 16x27
#729 @ 496,958: 13x22
#730 @ 139,648: 15x20
#731 @ 657,488: 23x16
#732 @ 397,588: 23x17
#733 @ 49,752: 21x22
#734 @ 273,21: 21x20
#735 @ 188,850: 14x27
#736 @ 292,636: 23x14
#737 @ 359,63: 22x12
#738 @ 729,276: 26x12
#739 @ 835,616: 11x16
#740 @ 271,382: 27x19
#741 @ 278,302: 17x18
#742 @ 493,496: 15x18
#743 @ 368,971: 19x27
#744 @ 748,885: 13x24
#745 @ 228,442: 25x22
#746 @ 528,407: 8x5
#747 @ 726,57: 14x27
#748 @ 485,120: 19x11
#749 @ 760,807: 24x16
#750 @ 626,804: 12x26
#751 @ 583,450: 23x14
#752 @ 875,297: 26x19
#753 @ 91,189: 22x29
#754 @ 411,211: 15x25
#755 @ 303,275: 22x20
#756 @ 576,393: 21x12
#757 @ 144,247: 24x24
#758 @ 731,681: 11x16
#759 @ 835,782: 13x27
#760 @ 884,323: 23x28
#761 @ 571,685: 29x26
#762 @ 405,838: 11x20
#763 @ 17,144: 11x19
#764 @ 35,679: 23x22
#765 @ 731,771: 10x25
#766 @ 212,104: 14x24
#767 @ 282,389: 10x12
#768 @ 706,508: 29x16
#769 @ 52,53: 27x20
#770 @ 164,148: 25x28
#771 @ 943,570: 13x20
#772 @ 310,677: 10x28
#773 @ 655,283: 22x25
#774 @ 814,799: 17x17
#775 @ 156,648: 25x22
#776 @ 339,693: 23x20
#777 @ 556,248: 12x20
#778 @ 47,192: 13x27
#779 @ 273,803: 24x17
#780 @ 232,738: 28x12
#781 @ 695,537: 23x12
#782 @ 913,8: 15x26
#783 @ 735,601: 26x12
#784 @ 848,30: 13x10
#785 @ 557,251: 27x12
#786 @ 767,693: 15x15
#787 @ 114,129: 27x11
#788 @ 904,782: 21x10
#789 @ 471,297: 13x24
#790 @ 48,66: 29x20
#791 @ 666,750: 18x23
#792 @ 618,646: 12x21
#793 @ 615,375: 16x6
#794 @ 643,176: 23x10
#795 @ 367,620: 28x22
#796 @ 354,572: 15x17
#797 @ 826,533: 20x24
#798 @ 820,942: 15x27
#799 @ 340,142: 23x23
#800 @ 206,71: 16x20
#801 @ 489,871: 28x19
#802 @ 637,169: 25x16
#803 @ 274,921: 13x10
#804 @ 481,15: 13x15
#805 @ 253,611: 20x28
#806 @ 602,785: 25x22
#807 @ 672,17: 14x13
#808 @ 661,293: 25x13
#809 @ 824,541: 28x11
#810 @ 136,415: 14x22
#811 @ 261,277: 13x15
#812 @ 669,187: 13x14
#813 @ 619,271: 23x25
#814 @ 731,726: 29x14
#815 @ 879,274: 17x15
#816 @ 629,69: 16x28
#817 @ 102,791: 19x21
#818 @ 267,846: 19x23
#819 @ 625,487: 23x11
#820 @ 507,846: 19x16
#821 @ 667,653: 17x14
#822 @ 572,536: 13x28
#823 @ 426,317: 21x28
#824 @ 747,595: 17x29
#825 @ 215,895: 18x17
#826 @ 73,623: 18x17
#827 @ 548,88: 22x20
#828 @ 255,409: 29x26
#829 @ 347,18: 13x12
#830 @ 501,952: 24x19
#831 @ 245,931: 25x25
#832 @ 570,879: 28x14
#833 @ 450,90: 20x26
#834 @ 720,927: 24x17
#835 @ 267,773: 28x28
#836 @ 641,282: 19x21
#837 @ 370,300: 19x27
#838 @ 562,573: 23x12
#839 @ 845,241: 23x17
#840 @ 747,396: 21x17
#841 @ 318,786: 12x28
#842 @ 618,639: 11x18
#843 @ 361,510: 20x21
#844 @ 216,104: 13x16
#845 @ 446,919: 10x12
#846 @ 226,835: 26x13
#847 @ 967,34: 18x29
#848 @ 890,380: 11x25
#849 @ 791,93: 22x22
#850 @ 660,411: 22x25
#851 @ 500,162: 15x3
#852 @ 93,268: 22x20
#853 @ 668,91: 17x24
#854 @ 571,33: 23x27
#855 @ 0,52: 21x14
#856 @ 814,34: 11x10
#857 @ 145,285: 21x19
#858 @ 944,909: 14x17
#859 @ 247,728: 17x26
#860 @ 728,34: 20x21
#861 @ 68,678: 20x24
#862 @ 361,403: 19x23
#863 @ 886,228: 13x18
#864 @ 669,590: 13x29
#865 @ 677,353: 15x18
#866 @ 489,415: 15x27
#867 @ 83,215: 18x18
#868 @ 462,538: 28x28
#869 @ 636,218: 10x14
#870 @ 844,484: 20x24
#871 @ 159,475: 14x26
#872 @ 454,171: 12x26
#873 @ 722,603: 20x22
#874 @ 242,1: 13x22
#875 @ 825,201: 25x25
#876 @ 718,124: 28x14
#877 @ 485,800: 19x26
#878 @ 788,519: 11x20
#879 @ 79,373: 25x27
#880 @ 332,334: 18x12
#881 @ 189,181: 16x17
#882 @ 479,377: 18x13
#883 @ 13,41: 20x17
#884 @ 45,863: 19x15
#885 @ 1,326: 12x14
#886 @ 583,503: 28x14
#887 @ 472,419: 19x21
#888 @ 894,264: 29x14
#889 @ 606,370: 29x17
#890 @ 527,750: 27x14
#891 @ 949,730: 23x28
#892 @ 909,671: 29x29
#893 @ 874,592: 19x18
#894 @ 397,600: 15x21
#895 @ 446,647: 13x20
#896 @ 347,123: 16x17
#897 @ 563,906: 12x15
#898 @ 42,224: 10x24
#899 @ 550,246: 23x28
#900 @ 551,859: 17x12
#901 @ 443,424: 16x17
#902 @ 495,422: 16x15
#903 @ 767,818: 28x17
#904 @ 688,291: 14x26
#905 @ 60,429: 27x25
#906 @ 186,728: 10x29
#907 @ 902,11: 3x20
#908 @ 207,344: 21x28
#909 @ 10,464: 17x16
#910 @ 357,337: 22x20
#911 @ 376,840: 28x23
#912 @ 653,59: 24x18
#913 @ 355,615: 29x19
#914 @ 31,837: 25x15
#915 @ 658,397: 29x16
#916 @ 921,3: 12x26
#917 @ 664,204: 15x27
#918 @ 154,835: 11x16
#919 @ 621,922: 13x18
#920 @ 66,749: 24x24
#921 @ 53,535: 22x23
#922 @ 784,424: 28x14
#923 @ 282,774: 18x17
#924 @ 573,193: 28x14
#925 @ 266,146: 14x19
#926 @ 783,677: 14x29
#927 @ 617,911: 12x15
#928 @ 519,191: 27x10
#929 @ 647,85: 12x25
#930 @ 39,437: 29x27
#931 @ 203,769: 25x11
#932 @ 592,276: 29x18
#933 @ 362,219: 27x21
#934 @ 645,202: 10x25
#935 @ 21,804: 17x28
#936 @ 64,814: 10x19
#937 @ 61,738: 19x24
#938 @ 307,56: 24x11
#939 @ 354,527: 16x15
#940 @ 62,406: 26x15
#941 @ 94,858: 29x13
#942 @ 567,404: 16x11
#943 @ 664,600: 16x16
#944 @ 45,536: 13x12
#945 @ 165,127: 10x11
#946 @ 982,629: 12x19
#947 @ 377,796: 15x20
#948 @ 776,587: 21x18
#949 @ 607,281: 29x10
#950 @ 957,902: 21x17
#951 @ 902,349: 22x29
#952 @ 227,95: 16x25
#953 @ 46,869: 18x20
#954 @ 455,74: 28x26
#955 @ 66,145: 24x27
#956 @ 588,116: 13x12
#957 @ 181,659: 23x12
#958 @ 586,803: 27x15
#959 @ 344,358: 19x18
#960 @ 849,797: 16x15
#961 @ 56,705: 22x21
#962 @ 656,496: 18x27
#963 @ 341,152: 20x27
#964 @ 673,774: 28x19
#965 @ 945,406: 25x15
#966 @ 612,286: 10x11
#967 @ 300,439: 29x25
#968 @ 927,894: 24x25
#969 @ 675,177: 12x16
#970 @ 177,709: 26x27
#971 @ 297,897: 10x16
#972 @ 477,891: 12x21
#973 @ 369,149: 12x26
#974 @ 345,337: 21x29
#975 @ 180,745: 28x18
#976 @ 166,127: 15x27
#977 @ 522,634: 16x19
#978 @ 137,538: 25x18
#979 @ 531,223: 11x19
#980 @ 42,398: 27x18
#981 @ 490,196: 28x20
#982 @ 528,625: 28x16
#983 @ 539,628: 14x20
#984 @ 408,500: 23x17
#985 @ 158,493: 12x14
#986 @ 371,777: 22x11
#987 @ 871,272: 13x11
#988 @ 358,756: 10x29
#989 @ 191,48: 18x26
#990 @ 27,392: 16x26
#991 @ 446,939: 20x23
#992 @ 691,395: 16x22
#993 @ 476,461: 19x24
#994 @ 855,573: 11x26
#995 @ 334,838: 13x20
#996 @ 140,614: 26x25
#997 @ 535,63: 16x25
#998 @ 0,604: 14x12
#999 @ 432,266: 26x14
#1000 @ 279,927: 11x11
#1001 @ 494,448: 12x20
#1002 @ 325,141: 27x21
#1003 @ 920,327: 25x10
#1004 @ 642,228: 24x12
#1005 @ 196,759: 17x19
#1006 @ 542,400: 26x12
#1007 @ 304,385: 28x23
#1008 @ 395,756: 24x22
#1009 @ 295,462: 28x28
#1010 @ 238,445: 20x20
#1011 @ 248,546: 28x22
#1012 @ 922,166: 13x25
#1013 @ 600,344: 19x10
#1014 @ 712,652: 13x16
#1015 @ 21,188: 12x18
#1016 @ 838,468: 11x15
#1017 @ 884,27: 22x27
#1018 @ 367,386: 15x27
#1019 @ 292,420: 29x12
#1020 @ 843,935: 10x27
#1021 @ 286,34: 23x18
#1022 @ 127,344: 21x10
#1023 @ 776,699: 22x25
#1024 @ 232,558: 13x11
#1025 @ 967,72: 25x24
#1026 @ 686,689: 17x27
#1027 @ 823,776: 10x28
#1028 @ 577,556: 25x28
#1029 @ 18,654: 14x16
#1030 @ 380,621: 26x22
#1031 @ 977,166: 21x28
#1032 @ 798,399: 25x27
#1033 @ 11,357: 13x16
#1034 @ 443,643: 14x26
#1035 @ 151,891: 15x18
#1036 @ 740,685: 29x16
#1037 @ 254,452: 25x15
#1038 @ 236,243: 27x16
#1039 @ 368,318: 11x24
#1040 @ 472,152: 16x21
#1041 @ 399,708: 11x16
#1042 @ 235,737: 17x10
#1043 @ 150,663: 23x19
#1044 @ 60,298: 22x28
#1045 @ 354,559: 23x21
#1046 @ 409,574: 19x20
#1047 @ 313,857: 27x29
#1048 @ 103,344: 11x10
#1049 @ 834,934: 11x24
#1050 @ 717,940: 13x11
#1051 @ 41,223: 10x18
#1052 @ 371,739: 27x28
#1053 @ 92,476: 15x22
#1054 @ 870,489: 10x15
#1055 @ 853,315: 27x24
#1056 @ 576,539: 29x23
#1057 @ 647,851: 11x29
#1058 @ 428,66: 15x14
#1059 @ 548,285: 14x19
#1060 @ 395,741: 14x11
#1061 @ 236,990: 22x10
#1062 @ 796,222: 22x23
#1063 @ 181,783: 11x10
#1064 @ 867,246: 20x23
#1065 @ 773,316: 14x18
#1066 @ 906,410: 21x21
#1067 @ 498,156: 25x14
#1068 @ 599,351: 25x13
#1069 @ 925,137: 28x12
#1070 @ 845,927: 13x11
#1071 @ 230,248: 12x20
#1072 @ 645,399: 10x15
#1073 @ 303,104: 22x25
#1074 @ 448,294: 26x22
#1075 @ 29,703: 25x26
#1076 @ 273,351: 14x29
#1077 @ 890,950: 21x23
#1078 @ 212,515: 16x18
#1079 @ 377,549: 20x19
#1080 @ 502,813: 21x11
#1081 @ 421,84: 16x21
#1082 @ 207,523: 14x29
#1083 @ 439,228: 25x27
#1084 @ 159,852: 18x23
#1085 @ 271,99: 26x27
#1086 @ 111,732: 10x13
#1087 @ 896,182: 27x25
#1088 @ 666,223: 12x17
#1089 @ 131,623: 12x19
#1090 @ 504,197: 21x17
#1091 @ 637,293: 19x27
#1092 @ 163,288: 19x17
#1093 @ 165,629: 14x23
#1094 @ 126,121: 25x29
#1095 @ 677,242: 18x23
#1096 @ 981,370: 12x26
#1097 @ 107,185: 19x17
#1098 @ 98,571: 15x28
#1099 @ 827,783: 22x18
#1100 @ 672,260: 24x22
#1101 @ 26,244: 18x15
#1102 @ 180,643: 18x22
#1103 @ 611,984: 24x10
#1104 @ 389,566: 25x18
#1105 @ 399,864: 19x27
#1106 @ 402,639: 26x16
#1107 @ 266,931: 21x25
#1108 @ 162,848: 12x12
#1109 @ 360,761: 17x28
#1110 @ 97,294: 17x23
#1111 @ 498,968: 25x16
#1112 @ 101,223: 14x18
#1113 @ 625,93: 11x19
#1114 @ 162,614: 11x20
#1115 @ 130,346: 17x17
#1116 @ 219,754: 21x17
#1117 @ 848,588: 12x10
#1118 @ 559,258: 10x13
#1119 @ 787,71: 12x24
#1120 @ 897,647: 29x24
#1121 @ 341,809: 21x22
#1122 @ 455,62: 25x18
#1123 @ 691,53: 24x17
#1124 @ 392,5: 13x26
#1125 @ 351,781: 11x29
#1126 @ 839,311: 15x13
#1127 @ 798,604: 23x23
#1128 @ 696,229: 21x21
#1129 @ 647,153: 20x23
#1130 @ 667,363: 21x22
#1131 @ 389,545: 21x24
#1132 @ 761,288: 17x24
#1133 @ 470,167: 21x29
#1134 @ 349,766: 18x25
#1135 @ 340,935: 20x26
#1136 @ 4,908: 21x29
#1137 @ 565,377: 21x12
#1138 @ 94,569: 11x14
#1139 @ 644,285: 12x16
#1140 @ 564,384: 12x20
#1141 @ 41,483: 27x11
#1142 @ 722,544: 28x10
#1143 @ 615,583: 9x3
#1144 @ 0,672: 29x12
#1145 @ 20,847: 24x16
#1146 @ 708,262: 24x20
#1147 @ 175,292: 21x26
#1148 @ 914,609: 24x14
#1149 @ 725,781: 11x21
#1150 @ 834,707: 26x28
#1151 @ 440,81: 16x23
#1152 @ 766,952: 22x19
#1153 @ 99,622: 17x26
#1154 @ 681,3: 23x28
#1155 @ 259,251: 17x10
#1156 @ 533,455: 18x12
#1157 @ 709,842: 12x27
#1158 @ 14,621: 25x14
#1159 @ 566,622: 17x29
#1160 @ 636,134: 25x29
#1161 @ 757,922: 22x12
#1162 @ 969,239: 22x13
#1163 @ 420,62: 26x23
#1164 @ 625,743: 14x28
#1165 @ 949,143: 10x25
#1166 @ 259,281: 20x14
#1167 @ 471,320: 13x28
#1168 @ 705,49: 14x26
#1169 @ 292,86: 15x16
#1170 @ 22,866: 19x29
#1171 @ 2,60: 17x25
#1172 @ 661,736: 16x18
#1173 @ 759,960: 10x10
#1174 @ 574,557: 15x14
#1175 @ 61,710: 18x12
#1176 @ 579,843: 26x22
#1177 @ 791,503: 26x13
#1178 @ 872,637: 22x29
#1179 @ 53,315: 12x29
#1180 @ 833,191: 14x12
#1181 @ 758,584: 28x12
#1182 @ 420,570: 23x12
#1183 @ 728,753: 11x12
#1184 @ 666,263: 24x24
#1185 @ 260,132: 15x29
#1186 @ 152,317: 27x26
#1187 @ 951,732: 13x14
#1188 @ 67,614: 21x14
#1189 @ 958,630: 22x11
#1190 @ 384,45: 24x25
#1191 @ 346,237: 14x28
#1192 @ 211,643: 29x27
#1193 @ 175,936: 15x22
#1194 @ 524,439: 19x18
#1195 @ 538,791: 14x14
#1196 @ 584,805: 17x25
#1197 @ 469,632: 12x28
#1198 @ 57,347: 22x14
#1199 @ 230,671: 20x15
#1200 @ 512,561: 27x28
#1201 @ 552,669: 26x23
#1202 @ 226,22: 21x24
#1203 @ 221,753: 28x20
#1204 @ 920,25: 24x12
#1205 @ 742,837: 13x14
#1206 @ 492,963: 29x10
#1207 @ 40,665: 23x27
#1208 @ 849,438: 10x17
#1209 @ 94,246: 13x25
#1210 @ 952,710: 28x23
#1211 @ 541,686: 24x23
#1212 @ 714,111: 16x24
#1213 @ 259,319: 16x22
#1214 @ 660,855: 26x15
#1215 @ 562,534: 28x29
#1216 @ 197,456: 27x18
#1217 @ 412,225: 28x22
#1218 @ 286,923: 12x15
#1219 @ 632,760: 27x29
#1220 @ 617,558: 26x15
#1221 @ 442,35: 17x28
#1222 @ 725,652: 12x11
#1223 @ 311,518: 26x17
#1224 @ 902,710: 23x29
#1225 @ 712,333: 12x17
#1226 @ 414,401: 20x14
#1227 @ 562,688: 13x15
#1228 @ 567,645: 14x15
#1229 @ 135,458: 22x18
#1230 @ 38,104: 26x29
#1231 @ 58,705: 29x12
#1232 @ 61,357: 21x15
#1233 @ 313,640: 11x19
#1234 @ 538,230: 18x29
#1235 @ 222,268: 13x20
#1236 @ 544,744: 17x22
#1237 @ 105,517: 25x18
#1238 @ 183,916: 20x22
#1239 @ 209,370: 13x12
#1240 @ 876,381: 21x10
#1241 @ 363,150: 15x13
#1242 @ 523,458: 17x25
#1243 @ 673,230: 12x17
#1244 @ 38,232: 9x10
#1245 @ 384,705: 22x26
#1246 @ 589,492: 25x22
#1247 @ 216,119: 26x21
#1248 @ 849,689: 21x22
#1249 @ 408,511: 24x28
#1250 @ 542,246: 21x13
#1251 @ 604,337: 18x13
#1252 @ 585,350: 14x18
#1253 @ 31,732: 24x12
#1254 @ 498,412: 21x19
#1255 @ 837,453: 18x28
#1256 @ 364,693: 18x19
#1257 @ 875,907: 17x17
#1258 @ 576,959: 26x10
#1259 @ 48,909: 16x18
#1260 @ 746,403: 24x22
#1261 @ 228,739: 13x18
#1262 @ 65,699: 25x18
#1263 @ 138,413: 10x12
#1264 @ 477,549: 28x18
#1265 @ 223,135: 12x28
#1266 @ 234,465: 10x11
#1267 @ 899,9: 11x25
#1268 @ 853,470: 19x14
#1269 @ 570,112: 23x20
#1270 @ 514,890: 19x15
#1271 @ 762,782: 19x23
#1272 @ 536,41: 15x29
#1273 @ 866,59: 16x14
#1274 @ 64,614: 19x19
#1275 @ 362,361: 11x25
#1276 @ 699,462: 18x27
#1277 @ 375,331: 19x19
#1278 @ 59,522: 17x28
#1279 @ 533,425: 14x25
#1280 @ 916,11: 4x19
#1281 @ 759,278: 23x14
#1282 @ 296,758: 25x22
#1283 @ 285,778: 11x22
#1284 @ 878,35: 15x28
#1285 @ 263,85: 24x23
#1286 @ 638,274: 19x24
#1287 @ 272,638: 10x28`

class Point =
	Var X: Int
	Var Y: Int

	Def new(x: Int, y: Int) =
		X = x
		Y = y

	Def ==(a: Point, b: Point) = (a.X == b.X && a.Y == b.Y)

	Def #(a: Point) = 31 * a.X ^ a.Y

	Def toString() = "(" + X + ", " + Y + ")"


val pointCount = new HashMap<Point, Int>()
val r = Pattern.compile(`\#(\d+) @ (\d+),(\d+)\: (\d+)x(\d+)`)

for(val line in input.Lines())
	val m = r.matcher(line)
	m.matches()

	val id = m.group(1).ToInt()
	val pos = new Point(m.group(2).ToInt(), m.group(3).ToInt())
	val size = new Point(m.group(4).ToInt(), m.group(5).ToInt())

	for(var x = pos.X; x < pos.X + size.X; x++)
		for(var y = pos.Y; y < pos.Y + size.Y; y++)
			val point = new Point(x, y)
			pointCount[point] = (pointCount.Get(point) ?: 0) + 1

var overlapping = 0
for(val entry in pointCount)
	if(entry.Value() > 1)
		overlapping++

println(overlapping) // res: 105047
