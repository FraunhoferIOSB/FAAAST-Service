https://www.modbus.org/modbus-specifications

coils, discrete inputs, holding registers, input registers

What does not work yet:

- Operations
- KeyManagerFactory for TLS connections
- Lots of DataElementValue types to write
- Only PropertyElements are supported as of now. Are more necessary? E.g., blobs are not really reasonable, as modbus is
  not designed for that.
- probably a lot more which I will discover when running the program
