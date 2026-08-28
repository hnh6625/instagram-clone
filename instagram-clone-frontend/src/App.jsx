import { useState, useEffect } from 'react'
import axios from 'axios'

function App() {
    const [pingResult, setPingResult] = useState(null)

    useEffect(() => {
        axios
            .get('http://localhost:8080/ping')
            .then((response) => {
                setPingResult(response.data)
        })
            .catch((error) => {
                console.error(error)
            })
    }, [])

    return (
        <div>
            <h1>Ping API</h1>
            {pingResult ? (
                <p>Status: {pingResult.status}</p>
            ) : (
                <p>Loading...</p>
            )}
        </div>
    )
}

export default App