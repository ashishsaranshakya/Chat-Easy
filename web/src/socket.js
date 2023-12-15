import { Server } from "socket.io";
import { verifyTokenSocket } from "./middlewares/auth.js";
import Chat from "./models/Chat.js";
import Message from "./models/Message.js";
import User from "./models/User.js";

const CHAT = "chat";
const CHATS = "chats";
const MESSAGE = "message";
const JOIN_CHAT = "join chat";
const LEAVE_CHAT = "leave chat";
const SINGLE_CHAT = "single chat";
const SEARCH = "search";
const SEARCH_GROUP = "search group";

export default (server) => {
	const io = new Server(server, {
		pingTimeout: 60000,
	});

	io.use(verifyTokenSocket);
	  
	io.on("connection", async (socket) => {
		console.log("Connected to socket.io: " + socket.user.userId);
		console.count(socket.user.userId);

		socket.on(MESSAGE, async (data) => {
			console.log(data);

			const chat = await Chat.findOne({ _id: data.chatId });
			if (chat.members.indexOf(socket.user.userId) === -1) {
				console.log("User not in chat");
				return;
			}

			const message = new Message({
				chat: data.chatId,
				sender: socket.user.userId,
				content: data.content
			});
			await message.save();

			chat.lastMessage = message._id;
			await chat.save();

			const user = await User.findById(socket.user.userId);
			message.sender = user.username;

			socket.to(data.chatId).emit(MESSAGE, {
				content: message.content,
				sender: user.username,
				chat: message.chat
			});
		});

		socket.on(CHATS, async () => {
			const chats = await Chat.find({ members: { $elemMatch: { $eq: socket.user.userId } } })
									.sort({ updatedAt: -1 })
									.exec();
			chats.forEach(chat => {
				socket.join(chat.id);
			});

			for (const chat of chats) {
				if (!chat.isGroup) {
					chat.name = (await User.findById(chat.members.find(member => member.toString() !== socket.user.userId.toString()))).username;
				}
				const lastMessage = await Message.findById(chat.lastMessage);
				socket.emit(CHATS, {
					chatId: chat.id,
					name: chat.name,
					lastMessage: lastMessage ? lastMessage.content : "",
					isGroup: chat.isGroup,
					isAdmin: chat.admin ? chat.admin.toString() === socket.user.userId.toString() : false,
					updatedAt: Number(chat.updatedAt)
				});
			}
		})

		socket.on(SINGLE_CHAT, async (data) => {
			const chat = await Chat.findById(data.chatId);
			const existingMessages = await Message.find({ chat: data.chatId });
			const messages = await Promise.all(existingMessages.map(async (message) => {
				const sender = await User.findById(message.sender);
				message.sender = sender.username;
				return {
					content: message.content,
					sender: sender.username
				};
			}));
			if (!chat.isGroup) chat.name = (await User.findById(chat.members.find(member => member.toString() !== socket.user.userId.toString()))).username;
			
			socket.emit(SINGLE_CHAT, {
				chatId: data.chatId,
				name: chat.name,
				messages
			});
		})		

		socket.on(SEARCH, async (data) => {
			if (data.query === "") return;
			const user = await User.findById(socket.user.userId);
			
			const chats = await Chat.find({
				members: {
					$elemMatch: { $eq: socket.user.userId },
					$size: 2
				}
			});
			const usersInChat = chats.map(chat => chat.members.find(member => member.toString() !== socket.user.userId.toString()));

			const users = await User.find({
				username: {
					$regex: data.query,
					$options: "i",
					$nin: [user.username]
				},
				_id: {
					$nin: usersInChat
				}
			});

			socket.emit(SEARCH, {
				success: true,
				users
			});
		});

		socket.on(SEARCH_GROUP, async (data) => {
			const { id, search } = data;
			if (search === "") return res.status(200).json({ success: true, users: [] });

			const chat = await Chat.findById(id);
			if (!chat) return next(createAPIError(400, false, 'Chat not found'));
			if (chat.admin.toString() !== socket.user.userId.toString())
				return next(createAPIError(403, false, 'You are not allowed to add members'));

			const users = await User.find({ username: { $regex: search, $options: 'i' } });
			const members = await Promise.all(chat.members.map(async (id) => id.toString()));

			const usersNotInChat = [];
			for (const user of users) {
				if (!members.includes(user._id.toString())) {
					usersNotInChat.push(user);
				}
			}
			
			socket.emit(SEARCH_GROUP, {
				success: true,
				users: usersNotInChat
			});
		})
		
		socket.on(JOIN_CHAT, (data) => {
			socket.join(data.chatId);
		});

		socket.on(LEAVE_CHAT, (data) => {
			socket.leave(data.chatId);
		});
	});
}
