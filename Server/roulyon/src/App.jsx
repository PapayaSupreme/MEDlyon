import { ComputeFullPath } from './function'
import EntryInterface from './EntryInterface'
import Failure from './Failure'
import './App.css'
import ErrorBoundary from './ErrorBoundary'

function App() {

  return (
    <>
      <ErrorBoundary fallback={<p>There was an error</p>}>
        <EntryInterface />
      </ErrorBoundary>
      <span className='border'></span>
      <button onClick={ComputeFullPath}>Find the path</button>
      <Failure />
    </>
  )
}

export default App
