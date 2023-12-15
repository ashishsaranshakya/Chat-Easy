import mongoose from 'mongoose';
import config from '../configs/config.js';

mongoose.connection.once('open', () => {
    console.log('MongoDB connection established successfully');
  });
  
mongoose.connection.on('error', (err) => {
    console.error(err);
});

export const mongoConnect = async () => {
    mongoose.connect(
        config.mongoDB.url
    );
}
  
export const mongoDisconnect = async () => {
    await mongoose.disconnect();
}