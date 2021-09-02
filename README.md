# Introduction

<img width="207" alt="스크린샷 2020-12-26 오후 1 46 14" src="https://user-images.githubusercontent.com/48302757/103145759-e4825780-4782-11eb-88f8-a74d1297fc8c.png">

This is the P2P program.<br>
Seeders share the file and leechers download it.<br>
Leechers can share file chunks with others like seeder.

# Usage

### Before start program
1. Create a folder with name "user_[userID]" (e.g., user_1) in "test" folder.
2. Write local IP address and port number of each user at configuration.txt (e.g., 127.0.0.1 4000)

### On program
1. Set user ID
    - If you are seeder, use user ID 0
    - Otherwise, use user ID among 1, 2, 3 or 4
2. Set file name
    - If you are seeder, use the file name which you want to upload
    - Otherwise, use the file name which you want to download
