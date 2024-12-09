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
      { name: 'Family', admin: users[0]._id, members: [users[0]._id, users[1]._id, users[5]._id] },
      { name: 'Friends', admin: users[3]._id, members: [users[3]._id, users[4]._id, users[5]._id] },
      { name: 'Work- Devs', admin: users[1]._id, members: [users[1]._id, users[2]._id, users[3]._id, users[4]._id] },
    ];

    const createdGroupChats = await Chat.insertMany(groupChats.map(chat => ({
      isGroup: true,
      ...chat,
    })));

    // Write messages for each chat
    const allChats = [...createdOneToOneChats, ...createdGroupChats];

    const messages = [];

    let temp = ["Hi, Bob!", "Hey there, Alice! How's your day going?",
      "Not bad. What about you?",
      "Pretty good, thanks! Have you seen the latest movie?",
      "Yes, watched it last weekend. It was fantastic! Have you seen it yet?",
      "Not yet, planning to watch it this Friday. What did you like the most about it?",
      "The storyline was captivating, and the special effects were impressive!",
      "Sounds exciting! I can't wait to see it. What other movies have you watched recently?",
      'Saw a comedy and a thriller. Both were pretty good.',
      'Nice choices! Anything else new with you?',]
    
    for (let i = 0; i < 10; i++) {
      messages.push({
        sender: users[i%2]._id,
        content: temp[i],
        chat: allChats[0]._id,
      });
    }

    messages.push({
      sender: users[1]._id,
      content: "Hi, David!",
      chat: allChats[2]._id,
    });
    messages.push({
      sender: users[3]._id,
      content: "How's your day going?",
      chat: allChats[2]._id,
    });

    const groupConversation = [
      "Hey, everyone! How's your day going?",
      "Hey Alice! Pretty good, just busy with work. How about you?",
      "Hey guys! I'm doing alright. Just chilling at home.",
      "That's nice! We should plan something fun for the weekend.",
      "Absolutely! Maybe we could plan a movie night or a small get-together.",
      "That sounds like a great idea! What do you think, Alice?",
      "Count me in! A movie night would be perfect for a relaxing weekend.",
      "Awesome! We can decide on the movie and snacks later this week.",
      "Definitely. By the way, did anyone watch that new series on Netflix?",
      "Not yet! I've heard good things about it though. Is it worth watching?",
      "I've seen a few episodes. It's pretty intriguing, might be worth a shot.",
      "Alright, I'll check it out. Thanks, guys!",
      "No problem! Let us know your thoughts once you start watching it.",
      "Absolutely. Always up for some show recommendations."
    ];

    let grp = [users[0], users[1], users[5]];

    for (let i = 0; i < groupConversation.length; i++) {
      messages.push({
        sender: grp[i%3]._id,
        content: groupConversation[i],
        chat: createdGroupChats[0]._id,
      });
    }

    const workDevGroupConversation = [
      "Good morning, team! How's everyone doing today?",
      "Morning, Bob! Doing well, just reviewing the new code changes.",
      "Hey, everyone! I'm debugging some issues from yesterday's merge.",
      "Hi, folks! I'm working on the UI updates for the upcoming release.",
      "Great to hear! Charlie, any blockers with the code review?",
      "Not really, just a few minor suggestions. Should be done soon.",
      "I think I've found the root cause. It was an oversight in the logic.",
      "Awesome, David! Let me know if you need any help with the UI.",
      "Thanks for the updates, team. We're making good progress.",
      "Absolutely! It's all coming together nicely.",
      "Agreed. Once these fixes are in, we'll be ready for testing.",
      "Looking forward to it! The UI changes are looking sleek, by the way.",
      "Appreciate the effort, Emma! We're almost there, team.",
      "Definitely. Let's keep up the momentum!"
    ];

    grp = [users[1]._id, users[2]._id, users[3]._id, users[4]._id];

    for (let i = 0; i < workDevGroupConversation.length; i++) {
      messages.push({
        sender: grp[i%4],
        content: workDevGroupConversation[i],
        chat: createdGroupChats[2]._id,
      });
    }

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
