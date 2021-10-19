package com.moralis.web3

import org.web3j.utils.Convert

class MoralisUnitConverter {
    companion object {
        fun convertETHToWei(value: String): String {
            val wei = Convert.toWei(value, Convert.Unit.ETHER)
            val weiString = Convert.toWei(value, Convert.Unit.ETHER).toString()
            return weiString
        }

//        fun FromWei(value:String) {
//            return Convert.fromWei(value, )
//        }
    }
}