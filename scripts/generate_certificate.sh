#!/bin/bash

# Define the output directory
output_directory="C:\Users\cha35985\IdeaProjects\FAAAST\certs"

# Generate a private key
openssl genpkey -algorithm RSA -out "${output_directory}/private.key"

# Generate a certificate signing request (CSR)
openssl req -new -key "${output_directory}/private.key" -out "${output_directory}/csr.csr"

# Generate a self-signed certificate using the CSR
openssl x509 -req -days 365 -in "${output_directory}/csr.csr" -signkey "${output_directory}/private.key" -out "${output_directory}/certificate.crt"

echo "Private key and self-signed certificate generated successfully."


## Run the following commands in the terminal accordingly in order to make the script executable and to run the script.

##chmod +x generate_certificate.sh
##./generate_certificate.sh
