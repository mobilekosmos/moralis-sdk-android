package com.moralis.web3


class MoralisTransferUtils {

    companion object {
        // TODO: "cons" only on primitive types allowed
//        private val supportedTypes = arrayOf("native", "erc20", "erc721", "erc1155")

        val ERC1155TransferABI = arrayOf(
            mapOf(
                "inputs" to arrayOf(
                    mapOf("internalType" to "address", "name" to "from", "types" to "address"),
                    mapOf("internalType" to "address", "name" to "to", "types" to "address"),
                    mapOf("internalType" to "uint256", "name" to "id", "types" to "uint256"),
                    mapOf("internalType" to "uint256", "name" to "value", "types" to "uint256"),
                    mapOf("internalType" to "bytes", "name" to "data", "types" to "bytes")
                ),
                "outputs" to arrayOf(
                    mapOf("name" to "", "type" to "bool")
                ),
                "name" to "safeTransferFrom",
                "type" to "function",
                "constant" to false,
                "payable" to false,
            ),
            mapOf(
                "inputs" to arrayOf(
                    mapOf("internalType" to "address", "name" to "from", "types" to "address"),
                    mapOf("internalType" to "address", "name" to "to", "types" to "address"),
                    mapOf("internalType" to "uint256", "name" to "id", "types" to "uint256"),
                    mapOf("internalType" to "uint256", "name" to "value", "types" to "uint256")
                ),
                "outputs" to arrayOf(
                    mapOf("name" to "", "type" to "bool")
                ),
                "name" to "transferFrom",
                "type" to "function",
                "constant" to false,
                "payable" to false,
            )
        )

        val ERC721TransferABI = arrayOf(
            mapOf(
                "inputs" to arrayOf(
                    mapOf("internalType" to "address", "name" to "from", "types" to "address"),
                    mapOf("internalType" to "address", "name" to "to", "types" to "address"),
                    mapOf("internalType" to "uint256", "name" to "tokenId", "types" to "uint256")
                ),
                "outputs" to arrayOf(
                    mapOf("name" to "", "type" to "bool")
                ),
                "name" to "safeTransferFrom",
                "type" to "function",
                "constant" to false,
                "payable" to false,
            ),
            mapOf(
                "inputs" to arrayOf(
                    mapOf("internalType" to "address", "name" to "from", "types" to "address"),
                    mapOf("internalType" to "address", "name" to "to", "types" to "address"),
                    mapOf("internalType" to "uint256", "name" to "tokenId", "types" to "uint256")
                ),
                "outputs" to arrayOf(
                    mapOf("name" to "", "type" to "bool")
                ),
                "name" to "transferFrom",
                "type" to "function",
                "constant" to false,
                "payable" to false,
            )
        )

        val ERC20TransferABI = arrayOf(
            mapOf(
                "constant" to false,
                "inputs" to arrayOf(
                    mapOf("name" to "_to", "type" to "address"),
                    mapOf("name" to "_value", "type" to "uint256")
                ),
                "name" to "transfer",
                "outputs" to arrayOf(
                    mapOf("name" to "", "type" to "bool"),
                ),
                "payable" to false,
                "stateMutability" to "nonpayable",
                "type" to "function",
            ),

            mapOf(
                "constant" to true,
                "inputs" to arrayOf(
                    mapOf("name" to "_owner", "type" to "address"),
                    mapOf("name" to "_value", "type" to "uint256")
                ),
                "name" to "balanceOf",
                "outputs" to arrayOf(
                    mapOf("name" to "balance", "type" to "uint256"),
                ),
                "payable" to false,
                "stateMutability" to "view",
                "type" to "function",
            )
        )

//        val tokenParams = mapOf(
//            SupportedTypes.NATIVE to mapOf(
//                "receiver" to "",
//                "amount" to ""
//            ),
//            SupportedTypes.ERC20 to mapOf(
//                "contractAddress" to "",
//                "receiver" to "",
//                "amount" to ""
//            ),
//            SupportedTypes.ERC721 to mapOf(
//                "contractAddress" to "",
//                "receiver" to "",
//                "tokenId" to ""
//            ),
//            SupportedTypes.ERC1155 to mapOf(
//                "contractAddress" to "",
//                "receiver" to "",
//                "tokenId" to "",
//                "amount" to ""
//            ),
//        )

//        fun validateInput(transferObject: Transaction.TransferObject) {
//            val errors: MutableList<String> = ArrayList()
//            val parameters = tokenParams[transferObject.type]
//            parameters?.keys?.forEach {
//                if (!isNotEmpty(transferObject)) {
//                    errors.add("$it is required");
//                }
//            }
//
//            if (errors.isNotEmpty()) {
////                TODO: throw errors;
//            }
//        }

//        module.exports =
//        {
//            abi: { erc1155: ERC1155TransferABI,
//                   erc721: ERC721TransferABI,
//                   erc20: ERC20TransferABI,
//        }
//            validateInput,
//            isSupportedType,
//            isNotEmpty,
//        }
    }
}