import React from 'react';
import { makeStyles, lighten, withStyles } from '@material-ui/core/styles';
import LinearProgress from '@material-ui/core/LinearProgress';

const useStyles = makeStyles(theme => ({
    root: {
        width: '100%',
        '& > * + *': {
            marginTop: theme.spacing(2),
        },
    },
}));

const BorderLinearProgress = withStyles({
    root: {
        height: 5,
        backgroundColor: lighten('#28ff1b', 0.5),
    },
    bar: {
        borderRadius: 20,
        backgroundColor: '#28ff1b',
    },
})(LinearProgress);

export default function LinearDeterminate(props) {
    const classes = useStyles();

    return (
            <div className={classes.root}>
                <BorderLinearProgress
                        className={classes.margin}
                        variant="determinate"
                        value={props.completed}
                />
            </div>
    );
}
