import mongoose from "mongoose";

const chatModel = mongoose.Schema(
  	{
    	isGroup: {
			type: Boolean,
			default: false
		},
		name: {
			type: String,
			default: ""
		},
		admin: {
			type: mongoose.Schema.Types.ObjectId,
			ref: 'User'
		},
		members: [{
			type: mongoose.Schema.Types.ObjectId,
			ref: 'User'
		}],
		lastMessage: {
			type: mongoose.Schema.Types.ObjectId,
			ref: 'Message',
			default: null
		}
  	},
	{
		timestamps: true,
	}
);

const Chat = mongoose.model("Chat", chatModel);
export default Chat;