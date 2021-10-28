java -jar openapi-generator-cli.jar generate ^
     -g kotlin ^
     --library jvm-retrofit2 ^
     -i "https://deep-index.moralis.io/api-docs/v2/swagger.json" ^
	 -p groupId=com.moralis ^
     -p artifactId=moralis-api-client-kotlin ^
     -p artifactVersion=1.0.0 ^
     -p basePackage=com.moralis.web3.restapisdk ^
     -p configPackage=com.moralis.web3.restapisdk.config ^
     -p apiPackage=com.moralis.web3.restapisdk.api ^
     -p modelPackage=com.moralis.web3.restapisdk.model ^
     -p sourceFolder=src/main/gen ^
     -p dateLibrary=java8 ^
     -p java8=true ^
	 -p useCoroutines=true
pause