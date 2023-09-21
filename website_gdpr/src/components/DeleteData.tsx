import React, { useState } from "react";
import TextField from '@mui/material/TextField';
import { Grid, Typography } from "@mui/material";
import Button from "@mui/material/Button";


function DeleteData() {
  const [email, setEmail] = useState('');

  const sendEmail = () => {
    if (!email) {
      return;
    }
    window.open(`mailto:binary0010productions@gmail.com?subject=Delete%20my%20account&body=I%20would%20like%20my%20LiteWeight%20account%20to%20be%20deleted.%20The%20email%20of%20my%20account%20is%20${email}`,
      '_blank');
  };

  return <>
    <Grid container justifyContent="center">
      <Grid item xs={12}>
        <Typography align='center'>
          Enter the email of your LiteWeight account and send your request to have your account and data deleted. This
          will open up your mail app to send the email.
        </Typography>
      </Grid>
      <Grid item xs={2}>
      </Grid>
      <Grid item mt={2} xs={8}>
        <TextField value={email}
          label="Email Address"
          fullWidth
          variant="outlined"
          onChange={(email) => setEmail(email.target.value)} />
      </Grid>
      <Grid item xs={2}>
      </Grid>
      <Grid item mt={2}>
        <Button variant="outlined" onClick={sendEmail}>REQUEST ACCOUNT DELETION</Button>
      </Grid>
    </Grid>
  </>;
}

export default DeleteData;
