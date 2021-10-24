package com.moralis.web3

import org.web3j.utils.Convert

class MoralisUnitConverter {
    companion object {
        // TODO: not sure why the lib uses BigDecimal instead of BigInteger if WEI never has decimals.
        // issues opened on lib's github.
        fun convertETHToWei(value: String): String {
            val wei = Convert.toWei(value, Convert.Unit.ETHER)
            val weiString = Convert.toWei(value, Convert.Unit.ETHER).toBigInteger().toString()
            return weiString
        }

        fun convertETHToWeiHex(value: String): String {
            val wei = Convert.toWei(value, Convert.Unit.ETHER)
            val weiString = "0x" + String.format("%X", wei.toBigInteger())
            return weiString
        }

//        fun FromWei(value:String) {
//            return Convert.fromWei(value, )
//        }
    }
}