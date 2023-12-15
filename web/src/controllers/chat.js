import User from '../models/User.js';
import Chat from '../models/Chat.js';
import { createAPIError } from '../utils/APIError.js';
import mongoose from 'mongoose';
import Message from '../models/Message.js';

export const createChat = async (req, res, next) => {
    try{
		const { userId } = req.params;

		const user = await User.findById(userId);
        if(!user){
            return next(createAPIError(400, false, 'User not found'));
		}

		const checkChat = await Chat.findOne({ members: { $all: [req.user.userId, userId] } });
		if(checkChat && checkChat.members.length===2) return next(createAPIError(400, false, 'Chat already exists'));
		
		const chat = new Chat({
			memebers: []
		})
		chat.members.push(new mongoose.Types.ObjectId(req.user.userId))
		chat.members.push(new mongoose.Types.ObjectId(userId))

		await chat.save();
		console.log({
			success: true,
			message: 'Chat created successfully',
			chatId: chat._id,
			chatName: user.username
		})
		console.log(user)

		res.status(201).json({
			success: true,
			message: 'Chat created successfully',
			chatId: chat._id,
			chatName: user.username
		});
    }
    catch(err){
        next(err);
    }
}

export const createGroup = async (req, res, next) => {
    try{
		const { name } = req.body;
		if(!name) return next(createAPIError(400, false, 'Name is required'));

		const chat = new Chat({
			memebers: [],
			isGroup: true,
			name,
			admin: req.user.userId
		})
		chat.members.push(new mongoose.Types.ObjectId(req.user.userId))
		await chat.save();
		console.log(chat.name)
		res.status(201).json({
			success: true,
			message: 'Chat created successfully',
			chatId: chat._id,
			chatName: chat.name
		});
    }
    catch(err){
        next(err);
    }
}

export const addToGroup = async (req, res, next) => {
	try{
		const { id } = req.params;
		const { userId } = req.body;
		if(!userId) return next(createAPIError(400, false, 'User ID is required'));

		const chat = await Chat.findById(id);
		if(!chat) return next(createAPIError(400, false, 'Chat not found'));

		if(chat.admin.toString() !== req.user.userId.toString()){
			return next(createAPIError(403, false, 'You are not allowed to add members'));
		}
		if(chat.members.includes(userId)){
			return next(createAPIError(400, false, 'User already in the group'));
		}

		chat.members.push(userId);
		await chat.save();
		res.status(201).json({success: true, message: 'User added to group successfully'});
	}
	catch(err){
		next(err);
	}
}

export const deleteChat = async (req, res, next) => {
	try {
		const { id } = req.params;
		const chat = await Chat.findById(id);
		if (!chat) return next(createAPIError(400, false, 'Chat not found'));
		if (chat.members.indexOf(req.user.userId) === -1)
			return next(createAPIError(403, false, 'You are not allowed to delete this chat'));

		chat.members = chat.members.filter(member => member.toString() !== req.user.userId.toString());
		if (chat.members.length < 2) {
			await Message.deleteMany({ chat: id });
			await Chat.deleteOne({ _id: id});
			return res.status(200).json({
				success: true,
				message: 'Chat deleted successfully',
				chatId: chat._id
			});
		}
		if (chat.admin.toString() === req.user.userId.toString()) {
			chat.admin = chat.members[0];
		}
		await chat.save();
		
		res.status(200).json({
			success: true,
			message: 'Chat deleted successfully',
			chatId: chat._id
		});
	}
	catch (err) {
		console.log(err);
		next(err);
	}
}

export const deleteGroup = async (req, res, next) => {
	try {
		const { id } = req.params;
		const chat = await Chat.findById(id);
		if (!chat) return next(createAPIError(400, false, 'Chat not found'));
		if (chat.admin.toString() !== req.user.userId.toString()) {
			next(createAPIError(403, false, 'You are not allowed to delete this chat'));
		}
		await Message.deleteMany({ chat: id });
		await Chat.deleteOne({ _id: id });
		
		res.status(200).json({
			success: true,
			message: 'Chat deleted successfully',
			chatId: chat._id
		});
	}
	catch (err) {
		console.log(err);
		next(err);
	}
}