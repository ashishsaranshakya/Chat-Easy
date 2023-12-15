import express from 'express';
import { createChat, createGroup, addToGroup, deleteChat, deleteGroup } from '../controllers/chat.js';
import { verifyToken } from '../middlewares/auth.js';

const router = express.Router();

router.post('/user/:userId', verifyToken, createChat);//create chat
router.post('/group', verifyToken, createGroup);//create group chat
router.patch('/group/:id', verifyToken, addToGroup);//add to group chat
router.delete('/:id', verifyToken, deleteChat);
router.delete("/group/:id", verifyToken, deleteGroup);

export default router;