import React, { useEffect, useState } from 'react';
import './App.css';

function App() {
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {});

    return (
            <div className="App">
                {isLoading && <div>Loading...</div>}
                {!isLoading && <div>Home Page</div>}
            </div>
    );
}

export default App;
