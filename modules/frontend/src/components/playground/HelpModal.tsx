import * as React from 'react';
import Menu from 'semantic-ui-react/dist/commonjs/collections/Menu/Menu';
import Button from 'semantic-ui-react/dist/commonjs/elements/Button/Button';
import Icon from 'semantic-ui-react/dist/commonjs/elements/Icon/Icon';
import Modal from 'semantic-ui-react/dist/commonjs/modules/Modal/Modal';

interface HelpModalState {
  isOpen: boolean;
}

export default class HelpModal extends React.Component<{}, HelpModalState> {
  state: HelpModalState = { isOpen: false };

  handleOpen = () => this.setState({ isOpen: true });

  handleClose = () => this.setState({ isOpen: false });

  render() {
    const icon = 'question circle outline';
    return (
      <React.Fragment>
        <Menu.Item name="help" onClick={this.handleOpen}>
          <Icon name={icon}/>
          Help
        </Menu.Item>
        <Modal open={this.state.isOpen} size="small" dimmer onClose={this.handleClose}>
          <Modal.Header>
            <Icon name={icon}/>
            Playground Help
          </Modal.Header>
          <Modal.Content>
            In the playground you can try out <code>tlang</code> in real time! Enter code in the editor and press
            the <strong>Run Code</strong> button to have it evaluated
            (or press <code>CTRL</code> + <code>ENTER</code>).

            If you're unsure what to write you can start out with some examples by selecting one in
            the <strong>Examples</strong> dropdown.
          </Modal.Content>
          <Modal.Actions>
            <Button color="green" inverted onClick={this.handleClose}>
              <Icon name="checkmark" /> Ok
            </Button>
          </Modal.Actions>
        </Modal>
      </React.Fragment>

    );
  }

}
