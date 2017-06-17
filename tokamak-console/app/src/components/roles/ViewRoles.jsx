import React from "react";
import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
import RoleTable from "./RoleTable.jsx";

class ViewRoles extends React.Component {

    render() {
        return (
            <div className="animated fadeIn">
            {this.props.roles &&
              <MuiThemeProvider>
                  <div className="table-container">
                    <h1>Account Roles</h1>
                    <RoleTable roles={this.props.roles} />
                  </div>
              </MuiThemeProvider>
            }
            {!this.props.roles &&
              <div className="error-page">
                <h2 className="error-title">You haven't created any roles yet</h2>
                <button className="tok-button center margin-top-50">Create Role</button>
              </div>
            }
            </div>
        );
    }
  
}

export default ViewRoles;