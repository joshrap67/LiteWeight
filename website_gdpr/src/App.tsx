import React from "react";
import { createTheme, ThemeProvider } from "@mui/material/styles";

import { HashRouter, Route, Routes } from "react-router-dom";
import "./index.css";
import { Container } from "@mui/material";
import Home from "./components/Home";
import DeleteData from "./components/DeleteData";
import NavBar from "./components/NavBar";

function App() {
  const theme = createTheme({
    palette: {
      primary: {
        main: "#2B8366",
        contrastText: "#fff",
      },
      secondary: {
        main: "#e91e63",
        contrastText: "#000",
      },
    },
  });
  return (
    <HashRouter>
      <ThemeProvider theme={theme}>
        <div className="app">
          <NavBar />
          <Container sx={{ mt: 2 }}>
            <Routes>
              <Route path="/" element={<Home />}></Route>
              <Route path="/home" element={<Home />}></Route>
              <Route path="/delete-data" element={<DeleteData />}></Route>
            </Routes>
          </Container>
        </div>
      </ThemeProvider>
    </HashRouter>
  );
}

export default App;
