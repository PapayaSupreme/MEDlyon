import { ComputeFullPath } from './function'
import EntryInterface from './EntryInterface'
import Failure from './Failure'
import './App.css'
import ErrorBoundary from './ErrorBoundary'
import { useState } from 'react'

function App() {
  const [failure, setfailure] = useState('')

  return (
    <>
      <form>
        <ErrorBoundary fallback={<p>There was an error</p>}>
          <EntryInterface />
        </ErrorBoundary>
        <span className='border'></span>
        <br />
        <button onClick={e=> {e.preventDefault();ComputeFullPath(setfailure)}}>Find the path</button>
      </form>
      <Failure failure={failure}/>
    </>
  )
}

export default App
