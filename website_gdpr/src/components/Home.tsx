import React from "react";
import Card from "@mui/material/Card";
import CardActions from "@mui/material/CardActions";
import CardContent from "@mui/material/CardContent";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";

function Home() {
  return (
    <Card>
      <CardContent>
        <Typography>
          LiteWeight is a lightweight workout manager that allows for workouts
          to be created and managed. Default exercises are provided, and users
          can create exercises as needed. There are also social aspects to the
          app where you can add friends and send workouts to other LiteWeight
          users.
        </Typography>
      </CardContent>
      <CardActions>
        <Button
          size="small"
          href="https://play.google.com/store/apps/details?id=com.joshrap.liteweight&hl=en&gl=US"
          target="_blank"
        >
          Google Play Link
        </Button>
      </CardActions>
    </Card>
  );
}

export default Home;
