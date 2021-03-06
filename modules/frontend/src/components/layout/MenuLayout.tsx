import 'components/layout/MenuLayout.less';
import Navbar from 'components/layout/Navbar';
import Logo from 'components/misc/Logo';
import * as React from 'react';

import Footer from 'components/layout/Footer';
import Layout from 'components/layout/Layout';
import StandardLayout from 'components/layout/StandardLayout';
import Grid from 'semantic-ui-react/dist/commonjs/collections/Grid/Grid';
import Button from 'semantic-ui-react/dist/commonjs/elements/Button/Button';
import Icon from 'semantic-ui-react/dist/commonjs/elements/Icon/Icon';
import Segment from 'semantic-ui-react/dist/commonjs/elements/Segment/Segment';

interface MenuLayoutProps {
  menu: (state: MenuLayoutState) => JSX.Element;
  content: (state: MenuLayoutState) => JSX.Element;
}

interface MenuLayoutState {
  menuVisible: boolean;
}

export default class MenuLayout extends React.Component<MenuLayoutProps, MenuLayoutState> {

  state: MenuLayoutState = {  menuVisible: true, };

  toggleVisibility = (): void => this.setState(prev => ({ menuVisible: !prev.menuVisible }));

  mobileLayout = () => {
    const { content } = this.props;
    return <StandardLayout bottom={false}>{content(this.state)}</StandardLayout>;
  }

  desktopLayout = () => {
    const { menu, content } = this.props;
    const { menuVisible } = this.state;

    // 15 px is the width of the scroll bar
    const rightSideStyle = menuVisible ? { width: 'calc(100% - 15px)' } : { borderLeft: 0 };
    const rightSide = (
      <React.Fragment>
        <Segment inverted id="MenuLayout-navbar">
          <Grid>
            <Grid.Column verticalAlign="middle">
              <Button icon onClick={this.toggleVisibility}>
                <Icon name={menuVisible ? 'angle double left' : 'angle double right'}/>
              </Button>
            </Grid.Column>
            <Grid.Column>
              <Navbar />
            </Grid.Column>
          </Grid>
        </Segment>
        <div id="MenuLayout-content" style={rightSideStyle}>
          {content(this.state)}
        </div>
      </React.Fragment>
    );

    if (!menuVisible) {
      return (
        <React.Fragment>
          {rightSide}
          <Footer bottom/>
        </React.Fragment>
      );
    }

    const leftSide = (
      <React.Fragment>
        <Segment inverted id="MenuLayout-logo">
          <Logo size={2.5}/>
        </Segment>
        <Segment inverted id="MenuLayout-menu">
          <div id="MenuLayout-menu-inner">
            {menu(this.state)}
          </div>
        </Segment>
      </React.Fragment>
    );

    return (
      <React.Fragment>
        <Grid>
          <Grid.Column width={3} id="MenuLayout-left">{leftSide}</Grid.Column>
          <Grid.Column width={13} id="MenuLayout-right">{rightSide}</Grid.Column>
        </Grid>
        <Footer bottom/>
      </React.Fragment>
    );
  }

  render() {
    return (
      <Layout
        mobile={this.mobileLayout}
        desktop={this.desktopLayout}
      />
    );
  }
}
