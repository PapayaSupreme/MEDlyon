
function Failure({failure}){
    if (failure !== ''){
        return <span className='failed'>{failure}</span>
    } else {
        return <></>
    }
}

export default Failure