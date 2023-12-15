import { mongoConnect, mongoDisconnect } from '../services/mongoDB.js';
import User from '../models/User.js';
import Chat from '../models/Chat.js';
import Message from '../models/Message.js';
import bcrypt from 'bcrypt';
import dotenv from 'dotenv';

dotenv.config();

const populateDB = async () => {
  try {
	await mongoConnect();
	
	await User.deleteMany({});
	await Chat.deleteMany({});
	await Message.deleteMany({});

    // Hashing the password using bcrypt
    const saltRounds = 10; // Adjust the salt rounds as needed

    const hashedPasswords = await Promise.all(
      ['Alice', 'Bob', 'Charlie', 'David', 'Emma', 'Frank'].map(async (username) => {
        const salt = await bcrypt.genSalt();
        const hashedPassword = await bcrypt.hash(username.toLowerCase(), salt);
        return hashedPassword;
      })
    );

    // Create users with realistic usernames and passwords
    const users = await User.insertMany([
      { username: 'Alice', password: hashedPasswords[0] },
      { username: 'Bob', password: hashedPasswords[1] },
      { username: 'Charlie', password: hashedPasswords[2] },
      { username: 'David', password: hashedPasswords[3] },
      { username: 'Emma', password: hashedPasswords[4] },
      { username: 'Frank', password: hashedPasswords[5] },
    ]);

    // Create one-to-one chats between users
    const oneToOneChats = [
      { members: [users[0]._id, users[1]._id] },
      { members: [users[0]._id, users[2]._id] },
      { members: [users[1]._id, users[3]._id] },
      { members: [users[2]._id, users[4]._id] },
    ];

    const createdOneToOneChats = await Chat.insertMany(oneToOneChats.map(chat => ({
      isGroup: false,
      ...chat,
    })));

    // Create group chats with different members
    const groupChats = [
      { name: 'Group 1', admin: users[0]._id, members: [users[0]._id, users[1]._id, users[2]._id] },
      { name: 'Group 2', admin: users[3]._id, members: [users[3]._id, users[4]._id, users[5]._id] },
      { name: 'Group 3', admin: users[1]._id, members: [users[1]._id, users[2]._id, users[3]._id, users[4]._id] },
    ];

    const createdGroupChats = await Chat.insertMany(groupChats.map(chat => ({
      isGroup: true,
      ...chat,
    })));

    // Write messages for each chat
    const allChats = [...createdOneToOneChats, ...createdGroupChats];

    const messages = [];

    allChats.forEach(chat => {
      for (let i = 0; i < 10; i++) {
        const randomUser = chat.members[Math.floor(Math.random() * chat.members.length)];
        messages.push({
          sender: randomUser._id,
          content: `Message ${i + 1} in ${chat.isGroup ? 'group' : 'chat'} ${chat._id}`,
          chat: chat._id,
        });
      }
    });

    const insertedMessages = await Message.insertMany(messages);

    // Update each chat's lastMessage with the most recent message
    const chatUpdatePromises = allChats.map(async (chat) => {
      const chatMessages = insertedMessages.filter(message => message.chat.equals(chat._id));
      const lastMessage = chatMessages.length > 0 ? chatMessages[chatMessages.length - 1]._id : null;
      await Chat.findByIdAndUpdate(chat._id, { lastMessage });
    });
    await Promise.all(chatUpdatePromises);

    // Disconnect from the MongoDB after population
    await mongoDisconnect();
  } catch (error) {
    console.error('Error populating the database:', error);
  }
};

// Run the population function
populateDB();
