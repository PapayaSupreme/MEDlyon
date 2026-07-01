import {
    failure,
    failuremsg
} from './function.jsx'

function Failure(){
    if (failure){
        return <span className='failed'>{failuremsg}</span>
    } else {
        return <></>
    }
}

export default Failure