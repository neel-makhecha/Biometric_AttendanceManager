# Biometric_AttendanceManager

#### Mobile Attendance Manager using Mantra MFS100 Fingerprint Recognition 

This demo app contains following features just to get started with using Mantra MFS100 fingerprint sensor with any Android app.

1. Add new users and enrol their fingerprint at the same time.
2. User data along with fingerprint data are stored and processed on-device (Though it is fairly easy to take everything to a remote server)
3. Just scan a finger on the connected fingerprint sensor and it will display the recognized user.

#### Fingerprint Data Storage
There are various ways of storing fingerprint data permanently either on-device or on remote database. The MFS100 sensor saves the digital image of the fingerprint on calling a method called AutoCapture() of MFS100 object on an instance of type FingerData which is passed as an argument in the same method, returning an integer. Along with the fingerprint image, FingerData instance also have various other attributes like Quality, Nfiq, etc. The image can be obtained in three formats, which are raw image format, bitmap or/and as an ISO template. These data can be stored merely as a byte[] array on a simple file and can be retrived easily. Another method of storing the fingerprint data is storing it on a relational database along with user IDs. The above application stores fingerprint in an SQLite database. This is done by obtaining ISO Template from FingerData instance and storing it in binary format into a byte array. The binary information is stored in the database in an attribute of blob type. Moreover, the fingerprint can also be stored and viewed as a Bitmap type. A static method of BitmapFactory called decodeByteArray accepts byte array, offset and length and decodes that byte array into Bitmap. matchISO of MFS100 instance can compare two ISO Templates (in byte array) and returns the score of matching as int. This application uses a score of 75 to consider a match, however it is recommended to keep the threshold score to 95-96 for more accurate results (This may lead to decreased number of fingerprint matches).
