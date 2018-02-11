package com.android.calculator2.utils;

import java.util.HashMap;

import android.util.Log;

import com.android.calculator2.R;

public class ContryFlag {

    private HashMap<String, Integer> contry_flag_map ;

    public ContryFlag() {
        contry_flag_map = new HashMap<String, Integer>();
        contry_flag_map.clear();
        contry_flag_map.put("AED", R.drawable.aed);
        contry_flag_map.put("AFG", R.drawable.afg);
        contry_flag_map.put("AFN", R.drawable.afn);
        contry_flag_map.put("AGO", R.drawable.ago);

        contry_flag_map.put("ALL", R.drawable.all);
        contry_flag_map.put("AMD", R.drawable.amd);
        contry_flag_map.put("ANG", R.drawable.ang);
        contry_flag_map.put("AOA", R.drawable.aoa);

        contry_flag_map.put("ARM", R.drawable.arm);
        contry_flag_map.put("ARS", R.drawable.ars);
        contry_flag_map.put("AUD", R.drawable.aud);
        contry_flag_map.put("AWG", R.drawable.awg);

        contry_flag_map.put("AZE", R.drawable.aze);
        contry_flag_map.put("AZN", R.drawable.azn);
        contry_flag_map.put("BAM", R.drawable.bam);
        contry_flag_map.put("BBD", R.drawable.bbd);

        contry_flag_map.put("BDT", R.drawable.bdt);
        contry_flag_map.put("BGN", R.drawable.bgn);
        contry_flag_map.put("BHD", R.drawable.bhd);
        contry_flag_map.put("BIF", R.drawable.bif);

        contry_flag_map.put("BIH", R.drawable.bih);
        contry_flag_map.put("BMD", R.drawable.bmd);
        contry_flag_map.put("BND", R.drawable.bnd);
        contry_flag_map.put("BOB", R.drawable.bob);

        contry_flag_map.put("BRL", R.drawable.brl);
        contry_flag_map.put("BSD", R.drawable.bsd);
        contry_flag_map.put("BTC", R.drawable.btc);
        contry_flag_map.put("BTN", R.drawable.btn);

        contry_flag_map.put("BWP", R.drawable.bwp);
        contry_flag_map.put("BYR", R.drawable.byr);
        contry_flag_map.put("BZD", R.drawable.bzd);
        contry_flag_map.put("CAD", R.drawable.cad);

        contry_flag_map.put("CDF", R.drawable.cdf);
        contry_flag_map.put("CHF", R.drawable.chf);
        contry_flag_map.put("CLP", R.drawable.clp);
        contry_flag_map.put("CNY", R.drawable.cny);

        contry_flag_map.put("COD", R.drawable.cod);
        contry_flag_map.put("COP", R.drawable.cop);
        contry_flag_map.put("CRC", R.drawable.crc);
        contry_flag_map.put("CUP", R.drawable.cup);

        contry_flag_map.put("CVE", R.drawable.cve);
        contry_flag_map.put("CZK", R.drawable.czk);
        contry_flag_map.put("DJF", R.drawable.djf);
        contry_flag_map.put("DKK", R.drawable.dkk);

        contry_flag_map.put("DOP", R.drawable.dop);
        contry_flag_map.put("DZD", R.drawable.dzd);
        contry_flag_map.put("ECS", R.drawable.ecs);
        contry_flag_map.put("EEK", R.drawable.eek);

        contry_flag_map.put("EGP", R.drawable.egp);
        contry_flag_map.put("ERN", R.drawable.ern);
        contry_flag_map.put("ETB", R.drawable.etb);
        contry_flag_map.put("EUR", R.drawable.eur);

        contry_flag_map.put("FJD", R.drawable.fjd);
        contry_flag_map.put("FKP", R.drawable.fkp);
        contry_flag_map.put("GBP", R.drawable.gbp);
        contry_flag_map.put("GEL", R.drawable.gel);

        contry_flag_map.put("GEO", R.drawable.geo);
        contry_flag_map.put("GHA", R.drawable.gha);
        contry_flag_map.put("GHC", R.drawable.ghc);
        contry_flag_map.put("GHS", R.drawable.ghs);

        contry_flag_map.put("GIP", R.drawable.gip);
        contry_flag_map.put("GMD", R.drawable.gmd);
        contry_flag_map.put("GNF", R.drawable.gnf);
        contry_flag_map.put("GTQ", R.drawable.gtq);

        contry_flag_map.put("GYD", R.drawable.gyd);
        contry_flag_map.put("HKD", R.drawable.hkd);
        contry_flag_map.put("HNL", R.drawable.hnl);
        contry_flag_map.put("HRK", R.drawable.hrk);

        contry_flag_map.put("HTG", R.drawable.htg);
        contry_flag_map.put("HUF", R.drawable.huf);
        contry_flag_map.put("IDR", R.drawable.idr);
        contry_flag_map.put("ILS", R.drawable.ils);

        contry_flag_map.put("INR", R.drawable.inr);
        contry_flag_map.put("IQD", R.drawable.iqd);
        contry_flag_map.put("IRR", R.drawable.irr);
        contry_flag_map.put("ISK", R.drawable.isk);

        contry_flag_map.put("JMD", R.drawable.jmd);
        contry_flag_map.put("JOD", R.drawable.jod);
        contry_flag_map.put("JPY", R.drawable.jpy);
        contry_flag_map.put("KAZ", R.drawable.kaz);

        contry_flag_map.put("KES", R.drawable.kes);
        contry_flag_map.put("KGS", R.drawable.kgs);
        contry_flag_map.put("KGZ", R.drawable.kgz);
        contry_flag_map.put("KHR", R.drawable.khr);

        contry_flag_map.put("KMF", R.drawable.kmf);
        contry_flag_map.put("KPW", R.drawable.kpw);
        contry_flag_map.put("KRW", R.drawable.krw);
        contry_flag_map.put("KWD", R.drawable.kwd);

        contry_flag_map.put("KYD", R.drawable.kyd);
        contry_flag_map.put("KZT", R.drawable.kzt);
        contry_flag_map.put("LAK", R.drawable.lak);
        contry_flag_map.put("LBP", R.drawable.lbp);

        contry_flag_map.put("LKR", R.drawable.lkr);
        contry_flag_map.put("LRD", R.drawable.lrd);
        contry_flag_map.put("LSL", R.drawable.lsl);
        contry_flag_map.put("LTL", R.drawable.ltl);

        contry_flag_map.put("LVL", R.drawable.lvl);
        contry_flag_map.put("LYD", R.drawable.lyd);
        contry_flag_map.put("MAD", R.drawable.mad);
        contry_flag_map.put("MDG", R.drawable.mdg);

        contry_flag_map.put("MDL", R.drawable.mdl);
        contry_flag_map.put("MGA", R.drawable.mga);
        contry_flag_map.put("MKD", R.drawable.mkd);
        contry_flag_map.put("MMK", R.drawable.mmk);

        contry_flag_map.put("MNT", R.drawable.mnt);
        contry_flag_map.put("MOP", R.drawable.mop);
        contry_flag_map.put("MOZ", R.drawable.moz);
        contry_flag_map.put("MRO", R.drawable.mro);

        contry_flag_map.put("MTL", R.drawable.mtl);
        contry_flag_map.put("MOP", R.drawable.mop);
        contry_flag_map.put("MOZ", R.drawable.moz);
        contry_flag_map.put("MRO", R.drawable.mro);

        contry_flag_map.put("MTL", R.drawable.mtl);
        contry_flag_map.put("MUR", R.drawable.mur);
        contry_flag_map.put("MVR", R.drawable.mvr);
        contry_flag_map.put("MWK", R.drawable.mwk);

        contry_flag_map.put("MXN", R.drawable.mxn);
        contry_flag_map.put("MYR", R.drawable.myr);
        contry_flag_map.put("MZN", R.drawable.mzn);
        contry_flag_map.put("NAD", R.drawable.nad);

        contry_flag_map.put("NGN", R.drawable.ngn);
        contry_flag_map.put("NIO", R.drawable.nio);
        contry_flag_map.put("NOK", R.drawable.nok);
        contry_flag_map.put("NPR", R.drawable.npr);

        contry_flag_map.put("NZD", R.drawable.nzd);
        contry_flag_map.put("OMR", R.drawable.omr);
        contry_flag_map.put("PAB", R.drawable.pab);
        contry_flag_map.put("PEN", R.drawable.pen);

        contry_flag_map.put("PGK", R.drawable.pgk);
        contry_flag_map.put("PHP", R.drawable.php);
        contry_flag_map.put("PKR", R.drawable.pkr);
        contry_flag_map.put("PLN", R.drawable.pln);

        contry_flag_map.put("PYG", R.drawable.pyg);
        contry_flag_map.put("QAR", R.drawable.qar);
        contry_flag_map.put("RON", R.drawable.ron);
        contry_flag_map.put("RSD", R.drawable.rsd);

        contry_flag_map.put("RUB", R.drawable.rub);
        contry_flag_map.put("RWF", R.drawable.rwf);
        contry_flag_map.put("SAR", R.drawable.sar);
        contry_flag_map.put("SBD", R.drawable.sbd);

        contry_flag_map.put("SCR", R.drawable.scr);
        contry_flag_map.put("SDG", R.drawable.sdg);
        contry_flag_map.put("SEK", R.drawable.sek);
        contry_flag_map.put("SGD", R.drawable.sgd);

        contry_flag_map.put("SHP", R.drawable.shp);
        contry_flag_map.put("SIT", R.drawable.sit);
        contry_flag_map.put("SKK", R.drawable.skk);
        contry_flag_map.put("SLL", R.drawable.sll);

        contry_flag_map.put("SOS", R.drawable.sos);
        contry_flag_map.put("SRB", R.drawable.srb);
        contry_flag_map.put("SRD", R.drawable.srd);
        contry_flag_map.put("STD", R.drawable.std);

        contry_flag_map.put("SUR", R.drawable.sur);
        contry_flag_map.put("SVC", R.drawable.svc);
        contry_flag_map.put("SYP", R.drawable.syp);
        contry_flag_map.put("SZL", R.drawable.szl);

        contry_flag_map.put("THB", R.drawable.thb);
        contry_flag_map.put("TJK", R.drawable.tjk);
        contry_flag_map.put("TJS", R.drawable.tjs);
        contry_flag_map.put("TKM", R.drawable.tkm);

        contry_flag_map.put("TMT", R.drawable.tmt);
        contry_flag_map.put("TND", R.drawable.tnd);
        contry_flag_map.put("TOP", R.drawable.top);
        contry_flag_map.put("TRY", R.drawable.trytry);

        contry_flag_map.put("TTD", R.drawable.ttd);
        contry_flag_map.put("TWD", R.drawable.twd);
        // contry_flag_map.put("TWD2", R.drawable.twd2);
        contry_flag_map.put("TZS", R.drawable.tzs);

        contry_flag_map.put("UAH", R.drawable.uah);
        contry_flag_map.put("UGX", R.drawable.ugx);
        contry_flag_map.put("USD", R.drawable.usd);
        contry_flag_map.put("UYU", R.drawable.uyu);

        contry_flag_map.put("UZB", R.drawable.uzb);
        contry_flag_map.put("UZS", R.drawable.uzs);
        contry_flag_map.put("VEF", R.drawable.vef);
        contry_flag_map.put("VND", R.drawable.vnd);

        contry_flag_map.put("VUV", R.drawable.vuv);
        contry_flag_map.put("WST", R.drawable.wst);
        contry_flag_map.put("XAF", R.drawable.xaf);
        contry_flag_map.put("XAG", R.drawable.xag);

        contry_flag_map.put("XAU", R.drawable.xau);
        contry_flag_map.put("XCD", R.drawable.xcd);
        contry_flag_map.put("XOF", R.drawable.xof);
        contry_flag_map.put("XPF", R.drawable.xpf);

        contry_flag_map.put("XPT", R.drawable.xpt);
        contry_flag_map.put("YER", R.drawable.yer);
        contry_flag_map.put("ZAR", R.drawable.zar);
        contry_flag_map.put("ZMW", R.drawable.zmw);

        contry_flag_map.put("ZWL", R.drawable.zwl);

    }

    public int getFlagIdByCurrencyCode(String name) {
        Integer id = contry_flag_map.get(name);
        if(id !=null){
            return id;
        } else {
            Log.i("zouxu", "not find name = "+name);
            return 0;
        }
    }

}
