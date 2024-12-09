# ChatEasy Android Application

ChatEasy is a real-time messaging Android application that utilizes a Node.js backend running both an Express server and a Socket.IO server. It enables users to communicate in real-time, create groups, and engage in one-to-one conversations.

## Features

- **Real-time Messaging:** Instant messaging functionality for seamless communication between users.
- **Group Creation:** Users can create groups for collaborative discussions and messaging.
- **Group Administration:** Group administrators can manage group settings and members.
- **Express Server:** Handles HTTP requests and API endpoints for user authentication and messaging functionalities.
- **Socket.IO Server:** Facilitates real-time bidirectional communication for instant messaging updates.

<details>
  <summary><h2>Preview Video</h2></summary>
  <video src="https://github.com/ashishsaranshakya/Chat-Easy/assets/74979286/94cbe006-942c-4f97-badf-f07dca0d464f"></video>
</details>


## Backend Setup

The backend is built using Node.js and utilizes Express for RESTful APIs and Socket.IO for real-time communication. To set up the backend:

1. **Clone the Repository:** Clone the backend repository.
2. **Install Dependencies:** Run `npm install` to install all necessary dependencies.
3. **Environment Variables:**: Create a `.env` file in the `web` directory and add the following.
```
MONGO_URL='<MongoDB_Url>'
PORT=3001
JWT_SECRET='512 bit Jwt_Secret'
API_VERSION='v1'
```
4. **Start Server:** Use `npm run dev` to start both the Express and Socket.IO servers.

## Running the Android App

To run the ChatEasy Android application:

1. **Clone the Repository:** Clone the Android application repository.
2. **Set Backend URL:** Set the backend URL in the app to communicate with the Node.js backend in `strings.xml`.
3. **Build and Run:** Build the Android app and run it on an Android emulator or a physical device.

## Tech Stack

- **Android:** Java for Android application development.
- **Node.js:** Backend server environment.
- **Express:** Backend server framework for handling HTTP requests.
- **Socket.IO:** Real-time bidirectional event-based communication.

Feel free to contribute, report issues, or suggest new features to enhance ChatEasy!

