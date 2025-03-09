# 🪓 Woodcutter Bot - RuneMate

This repository provides a **fully structured development environment** for RuneMate bots, including a **woodcutter bot** to learn from.

## 🚀 Getting Started

### ✅ Prerequisites
- **IntelliJ IDEA** (Recommended IDE) → [Download Here](https://www.jetbrains.com/idea/download/?section=windows)
- **Java JDK 17** (You can install this automatically upon making a New Java Project in IntelliJ by selecting the JDK dropdown list and selecting JDK 17 (Temurin).)
- **RuneMate Client** (Download from [RuneMate](https://www.runemate.com))

---

## 🔧 Installation & Setup

### **1️⃣ Clone the Repository**
When you first open IntelliJ you will see this 

![Image](https://github.com/user-attachments/assets/f543da5f-4436-446a-8d2c-b2f5abbceed1)

You want to click "Clone Repository" and drop this link inside: https://github.com/DaBest890/cody-bots

![Image](https://github.com/user-attachments/assets/de0488cf-9421-4b7d-a322-34e77eeba1eb)

Remember to configure your directory correctly as you will need to find this as you develop. My directory for my projects is: C:\Users\Maximo\IdeaProjects and I am cloning the project inside the body-bots project folder

Ensure the project location is correct


2️⃣ Run the Bot
To launch the bot in RuneMate, open the IntelliJ Terminal (Shortcut: CTRL + `) and run:

``` 
./gradlew runClient
```

RuneMate should now open, and you should see the woodcutter bot in the bot list.

3️⃣ Find the Bot

Go back to IntelliJ and copy the absolute path for "C:\Users\Your-User-Name\Your-Projects-Folder\Your-Project-Name\build\libs" This is where your built bots are put. 

![Image](https://github.com/user-attachments/assets/4d4c45f8-cecc-4618-a3a2-1dd67a7d9db1)

After you copy this absolute path paste it into RuneMate's local bot directories and click Select Folder.

![Image](https://github.com/user-attachments/assets/799dd74e-1da4-4d8d-9e5d-8611af7f5aff)

### ***Congratulations***
You now have a working woodcutting bot to work with, and an environment to develop bots in.



### ***❓ Troubleshooting ***
Bot Not Appearing in RuneMate?
Check if the compiled bot is in the correct directory:
```
C:\Users\Your-User-Name\Your-Projects-Folder\Your-Project-Name\build\libs
```

Make sure Gradle built the bot successfully:
``` 
./gradlew build
```

Restart RuneMate and try again.
By default, RuneMate bots are stored in:

```
C:\Users\Your-User-Name\Your-Projects-Folder\Your-Project-Name\build\libs
```
You can set a different location, but make sure to adjust paths accordingly.


📌 Summary
✅ Clone the repo
✅ Set up IntelliJ
✅ Run ./gradlew runClient to launch the bot
✅ Check build/libs if the bot doesn’t appear in RuneMate
🚀 Now you’re ready to start bot development! 🔥
