import mongoose from "mongoose";

const userModel = mongoose.Schema(
	{
		username: {
			type: String,
			requried: true,
		},
		password: {
			type: String,
			requried: true,
		},
	},
	{
		timestamps: true,
	}
);

const User = mongoose.model("User", userModel);
export default User;