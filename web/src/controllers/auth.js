import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import User from '../models/User.js';
import { createAPIError } from '../utils/APIError.js';

/* CREATE TOKEN */
export const createToken = (id) => {
    return jwt.sign(
        {
            userId: id
        },
        process.env.JWT_SECRET,
        {
            expiresIn:"7d",
            algorithm: 'HS512'
        }
    )
}

/* REGISTER */
export const register = async (req, res, next) => {
    try{
        const {username, password} = req.body;

        const user=await User.findOne({username});
        if(!!user){
            return next(createAPIError(400, false, 'Username already taken'));
        }

        const salt= await bcrypt.genSalt();
        const hashedPassword = await bcrypt.hash(password, salt);

        const newUser = new User({
            username,
            password: hashedPassword
        });
        await newUser.save();
        res.status(201).json({success: true, message: 'User created successfully'});
    }
    catch(err){
        next(err);
    }
}

/* LOGIN */
export const login = async (req, res, next) => {
    try{
        const {username, password} = req.body;
        
        const user=await User.findOne({username},{createdAt:0,updatedAt:0,__v:0});
        if(!user) {
            return next(createAPIError(400, false, "User not found"));
        }
        
        const isMatch = await bcrypt.compare(password, user.password);
        if(!isMatch){
            return next(createAPIError(400, false, "Invalid credentials"));
        }

        const token = createToken(user._id.toString());
        res.status(200).json({
            success: true,
            user: {
                username: user.username,
                _id: user._id,
                token
            }
        });
    }
    catch(err){
        next(err);
    }
}