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
    if (inputcount >= Nodes.length) setCount(inputcount-1)
  }

  return (
    <form>
        <Inputs count={inputcount}></Inputs>
        <button className='addInput' onClick={addinput}>+</button>
        <button className='removeInput' onClick={removeinput}>-</button>
        {/**You can always add more input but you can only remove one if there is no Node. */}
    </form>
  )
}

function Inputs({ count }){
    const Ipts= []
    for (let i = 1; i <= count ; i++) {
        if (i < Nodes.length){
            Ipts.push(singularInput(Nodes[i].name));
        } else {
            Ipts.push(singularInput(null))
        }
    }
    return Ipts;
}

function singularInput(name, i){
    if (name){
        return <input type='text' onBlur={e => {if(e.target.value!=name) {setNewPos(e.target.value,i)}}}>{name}</input>
    } else {
        return <input type='text' onBlur={e => {if(e.target.value!='') {setNewPos(e.target.value,i)}}}></input>
    }
}

export default EntryInterface 
