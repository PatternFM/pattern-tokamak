import React from 'react';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';

import Layout from "../layout/Layout.jsx";
import Loader from "../layout/Loader.jsx";
import ViewScopes from "./ViewScopes.jsx";
import ScopeService from "../../services/ScopeService.js";
import ApplicationError from "../error/ApplicationError.jsx";

class Scopes extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            scopes: [],
            loading: false,
            error: null
        };
    }

    componentWillMount() {
        this.setState({ loading:true });
        ScopeService.list().then((result) => {
            if(result.status === "accepted") {
                this.setState({scopes: result.instance.scopes}, function() { });
            }
            else {
                this.setState({ error:result.errors[0] });
            }
            this.setState({ loading:false });
        });
    }

    render() {
        let output = this.state.error != null ? <ApplicationError error={this.state.error} /> : <ViewScopes scopes={this.state.scopes} />;
        let render = this.state.loading ? <Loader /> : output;
        
        return (
            <Layout>
                <MuiThemeProvider>
                   <div className="content-holder">
                     {render}
                   </div>
                </MuiThemeProvider>
            </Layout>
        );
    }
    
}

export default Scopes;