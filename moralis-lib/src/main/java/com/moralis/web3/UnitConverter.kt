package com.moralis.web3

import org.web3j.utils.Convert

class UnitConverter {
    companion object {
        fun convertETHToGwei(value: String): String {
            return Convert.toWei(value, Convert.Unit.ETHER).toString()
        }

//        fun FromWei(value:String) {
//            return Convert.fromWei(value, )
//        }
    }
}