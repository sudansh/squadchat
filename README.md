## Features
- The conversation message view with basic text message support.
- Basic keyboard accessory for sending messages. Tapping the send button should add the message to the conversation view. Double tapping the send button should trigger a dummy message to be added from the remote user to simulate an incoming message.
- Message attributions(profile pic and name label) 
- As per the UI profile image is only shown for incoming messages. And every message has date of sending with it. 
But I was unsure from the UI when the time is shown. So I am showing the time if it's a new day and on every 5th message

### Additional features
- you can send multiline texts
- message adding animation of slideup
- photo and emoji supported, any new type can be added by extending the IMessage
- on long click on message you can copy or delete (like whatsapp)
- link parsing
- to check the chat with 3 people comment out line 37 and comment line 35