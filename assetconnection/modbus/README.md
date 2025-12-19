https://www.modbus.org/modbus-specifications

coils, discrete inputs, holding registers, input registers

What does not work yet:

- Operations
- KeyManagerFactory for TLS connections
- Only PropertyElements, Blobs and Files are supported as of now. Are more necessary? think of intent and uses of modbus.
- probably a lot more which I will discover when running the program

- Float/Double conversion from/to byte arrays. How should they be encoded? How does a server read them?
- Could provide bcd and IEEE754 encodings, but necessary?
