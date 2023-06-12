
set OPENSSL_CONF=C:\Program Files\OpenSSL-Win64

set KEY_PATH=C:\Users\cha35985\IdeaProjects\FAAAST\certs\private.key
set CERT_PATH=C:\Users\cha35985\IdeaProjects\FAAAST\certs\certificate.crt

openssl req -x509 -newkey rsa:2048 -nodes -keyout %KEY_PATH% -out %CERT_PATH% -days 365 -subj "/CN=localhost"

echo Private key and certificate generated successfully.