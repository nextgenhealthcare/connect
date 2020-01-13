keytool -importcert -file mirth_Connect_CodeSign.cer -alias MirthCodeSign -keystore "C:\Program Files\Java\jdk1.8.0_192\jre\lib\security\cacerts" -storepass changeit

#Exports private and public key from pfx(p12), pass required 
#--> openssl pkcs12 -in 3226.p12 -out converted-file.pem -nodes