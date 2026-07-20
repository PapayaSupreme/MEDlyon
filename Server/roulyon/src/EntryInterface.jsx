import { useState } from 'react'
import { 
    Nodes,
    setNewPos
 } from './function'

function EntryInterface() {
  const [inputcount, setCount] = useState(Nodes.length < 2 ? 2 : Nodes.length)

  function addinput(){
    setCount(inputcount+1)
  }

  function removeinput(){
    if (inputcount>2){
        if (inputcount === Nodes.length) {
            if (!confirm("You will be removing the last Node that you created, are you sure?")) return;
            Nodes.pop()
        }
        setCount(inputcount-1)
    }
  }

  return (
    <>
        <Inputs count={inputcount} setCount={setCount}></Inputs>
        <button className='addInput' onClick={e => {e.preventDefault();addinput()}}>+</button>
        <button className='removeInput' onClick={e => {e.preventDefault();removeinput()}}>-</button>
        {/**You can always add more input, but you can only remove one if there is no Node. */}
    </>
  )
}

function Inputs({ count , setCount}){
    const Ipts= []
    if (count < Nodes.length) setCount(Nodes.length);
    for (let i = 0; i < count ; i++) {
        if (i < Nodes.length){
            Ipts.push(singularInput(Nodes[i].name, i));
        } else {
            Ipts.push(singularInput('', i))
        }
    }
    return <div>{Ipts}</div>;
}

function singularInput(name, i){
    return (
        <label htmlFor={`node-${i}`}>
            <input
                type='text'
                id={`node-${i}`}
                defaultValue={name}
                onBlur={e => {
                    const value = e.target.value.trim()
                    if (value !== '' && value !== name) {
                        setNewPos(value, i)
                    }
                }}
            />
        </label>
    )
}

export default EntryInterface 
